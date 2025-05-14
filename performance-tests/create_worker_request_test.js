import http from 'k6/http';
import { check, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

export const options = {
    scenarios: {
        create_worker_requests_scenario: { // 시나리오 이름 명확화
            executor: 'constant-arrival-rate',
            rate: 200,                         // 초당 요청 수 (RPS)
            timeUnit: '1s',
            duration: '1m',                    // 테스트 지속 시간 (1분)
            preAllocatedVUs: 100,              // 초기 VU (rate와 응답시간 고려하여 설정)
            maxVUs: 300,                       // 최대 VU
        },
    },
    thresholds: {
        'http_req_duration{scenario:create_requests_scenario}': ['p(95)<1000'], // 95% 요청은 1초 안에 응답
        'http_req_failed{scenario:create_requests_scenario}': ['rate<0.05'],    // 실패율 5% 미만 (중복 생성 시도 등으로 인한 실패 감안)
        'checks{scenario:create_requests_scenario}': ['rate>0.95'],             // 성공적인 체크 비율 95% 이상
        'created_worker_requests_total{scenario:create_requests_scenario}': ['count>=10000'], // 1분간 약 12000개 요청 중 성공 예상치
    },
};

// --- API 기본 URL ---
const BASE_URL = 'http://localhost:8080'; // 실제 애플리케이션 주소로 변경

// --- 테스트 데이터 ID 범위 및 목록 ---
const MAX_WORKER_ID_IN_DB = 2000000; // DB에 생성된 총 근로자 수

const NEIGHBORHOOD_IDS = Array.from({ length: 20 }, (_, i) => i + 1); // 1부터 20까지
const JOB_TYPE_IDS = Array.from({ length: 5 }, (_, i) => i + 1);     // 1부터 5까지

// --- 사용자 정의 메트릭 ---
const reqDurationMetric = new Trend('req_duration_ms_create_worker_request');
const failRateMetric = new Rate('req_fail_rate_create_worker_request');
const successRateMetric = new Rate('req_success_rate_create_worker_request');
const createdCounterMetric = new Counter('created_worker_requests_total');
const duplicateAttemptErrorMetric = new Counter('duplicate_creation_attempt_errors');

// 고유한 (workerId, requestDate) 조합 생성을 위한 전역 카운터 (단일 k6 인스턴스 내에서만 유효)
// k6는 상태를 공유하지 않으므로, 분산 실행 시에는 이 방식으로는 완벽한 고유성 보장 불가
let globalRequestCounter = 0;

// --- k6 기본 실행 함수 ---
export default function () {
    group('API_Create_WorkerRequest', function () {
        const url = `${BASE_URL}/api/worker-request`;

        // --- 고유한 (workerId, requestDate) 조합 생성 시도 ---
        // globalRequestCounter는 0부터 (rate * duration - 1)까지 증가할 것으로 예상.
        // 이를 이용하여 workerId와 requestDate를 생성.
        const currentRequestIndex = globalRequestCounter++; // 다음 요청을 위해 증가

        // workerId: 1부터 MAX_WORKER_ID_IN_DB까지 순환하도록 설정
        // (currentRequestIndex % MAX_WORKER_ID_IN_DB) 결과는 0 ~ (MAX_WORKER_ID_IN_DB-1)
        // 따라서 +1을 하여 1 ~ MAX_WORKER_ID_IN_DB 범위로 만듦
        const workerIdToUse = (currentRequestIndex % MAX_WORKER_ID_IN_DB) + 1;

        // requestDate: workerIdToUse를 기반으로 오늘부터 29일 후까지의 날짜 중 하나로 분산
        // 이렇게 하면 동일 workerId에 대해 날짜가 다르게 생성될 가능성이 높아짐.
        const requestDate = new Date(); // 테스트 실행 시점의 현재 날짜
        // (workerIdToUse를 사용하는 것보다 currentRequestIndex를 사용하는 것이 날짜 분산에 더 좋을 수 있음)
        // 예: requestDate.setDate(requestDate.getDate() + (currentRequestIndex % 30));
        // 여기서는 테스트의 각 반복이 다른 날짜를 갖도록 하기 위해 currentRequestIndex 사용
        requestDate.setDate(requestDate.getDate() + (currentRequestIndex % 30)); // 0~29일 더함
        const formattedRequestDate = requestDate.toISOString().split('T')[0];


        const payload = JSON.stringify({
            workerId: workerIdToUse,
            requestDate: formattedRequestDate,
            neighborhoodIds: getRandomSubarray(NEIGHBORHOOD_IDS, Math.floor(Math.random() * 2) + 1), // 1~2개
            jobTypeIds: getRandomSubarray(JOB_TYPE_IDS, Math.floor(Math.random() * 2) + 1),       // 1~2개
        });

        const params = {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'CreateWorkerRequest_AttemptUniquePayload' },
        };

        const res = http.post(url, payload, params);

        // 메트릭 기록
        reqDurationMetric.add(res.timings.duration);
        const isSuccess = res.status === 200; // Controller가 ResponseEntity.ok() 사용
        failRateMetric.add(!isSuccess);
        successRateMetric.add(isSuccess);

        if (isSuccess) {
            createdCounterMetric.add(1);
        } else {
            // 실패 원인 중 중복 생성 시도인지 확인 (서버 응답 본문 내용에 따라 조건 수정 필요)
            if (res.status === 500 || res.status === 400) { // 예: IllegalStateException이 500으로, 또는 커스텀 예외가 400으로
                if (res.body && typeof res.body === 'string' && res.body.includes("Worker already has a request for this date")) {
                    duplicateAttemptErrorMetric.add(1);
                }
            }
        }

        // 응답 검증
        check(res, {
            '[CreateWorkerRequest] status is 200 (OK)': (r) => r.status === 200,
            '[CreateWorkerRequest] response contains workerRequestIds array': (r) => {
                if (!isSuccess) return true; // 실패한 요청은 이 체크는 통과시키고, failRateMetric으로 잡음
                try {
                    const json = r.json();
                    return Array.isArray(json.workerRequestIds) && json.workerRequestIds.length > 0;
                } catch (e) {
                    // console.error(`JSON parsing error for workerId ${workerIdToUse}, date ${formattedRequestDate}: ${e}`);
                    return false;
                }
            },
        });
    });
}

// 배열에서 랜덤하게 n개의 요소를 선택하는 헬퍼 함수 (이전과 동일)
function getRandomSubarray(arr, size) {
    if (!arr || arr.length === 0) return [];
    const actualSize = Math.min(size, arr.length);
    if (actualSize === 0) return [];
    const shuffled = arr.slice(0);
    let i = arr.length;
    const min = i - actualSize;
    let temp, index;
    while (i-- > min) {
        index = Math.floor((i + 1) * Math.random());
        temp = shuffled[index];
        shuffled[index] = shuffled[i];
        shuffled[i] = temp;
    }
    return shuffled.slice(min);
}