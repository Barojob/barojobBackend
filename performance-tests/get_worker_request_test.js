import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 테스트 설정
export const options = {
    scenarios: {
        get_worker_requests_scenario: { // 시나리오 이름 명확화
            executor: 'constant-arrival-rate',
            rate: 200,                         // 초당 요청 수 (RPS)
            timeUnit: '1s',
            duration: '1m',                    // 테스트 지속 시간 (1분)
            preAllocatedVUs: 100,              // 초기 VU (rate와 응답시간 고려하여 설정)
            maxVUs: 300,                       // 최대 VU
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<200'], // 95% 요청은 200ms 안에 응답해야 함
        'http_req_failed': ['rate<0.01'],   // 실패율은 1% 미만이어야 함
        'checks': ['rate>0.99'],            // 성공적인 체크 비율은 99% 이상
    }
};

// API 기본 URL
const BASE_URL = 'http://localhost:8080'; // 실제 애플리케이션 주소로 변경하세요.

// 테스트에 사용할 데이터 (더미 데이터 생성 스크립트 참고)
// 실제 DB에 있는 ID와 날짜를 사용하세요.
const NEIGHBORHOOD_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]; // 예시: 1부터 20까지
const JOB_TYPE_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]; // 예시: 1부터 10까지

// 오늘부터 한 달 뒤까지의 날짜 배열 생성 (YYYY-MM-DD 형식)
const TARGET_DATES = [];
const today = new Date();
for (let i = 0; i < 30; i++) {
    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + i);
    TARGET_DATES.push(targetDate.toISOString().split('T')[0]);
}

// 사용자 정의 메트릭
const getWorkerRequestsDuration = new Trend('get_worker_requests_duration');
const getWorkerRequestsFailRate = new Rate('get_worker_requests_fail_rate');
const getWorkerRequestsSuccessRate = new Rate('get_worker_requests_success_rate');


export default function () {
    group('GetWorkerRequestsAPI', function () {
        // 요청 파라미터 랜덤 선택
        const params = {};
        const queryParams = [];

        // 50% 확률로 neighborhoodIds 파라미터 추가 (1~3개 랜덤 선택)
        if (Math.random() < 0.5) {
            const count = Math.floor(Math.random() * 3) + 1;
            const selectedNeighborhoods = getRandomSubarray(NEIGHBORHOOD_IDS, count);
            selectedNeighborhoods.forEach(id => queryParams.push(`neighborhoodIds=${id}`));
        }

        // 50% 확률로 jobTypeIds 파라미터 추가 (1~3개 랜덤 선택)
        if (Math.random() < 0.5) {
            const count = Math.floor(Math.random() * 3) + 1;
            const selectedJobTypes = getRandomSubarray(JOB_TYPE_IDS, count);
            selectedJobTypes.forEach(id => queryParams.push(`jobTypeIds=${id}`));
        }

        // 50% 확률로 targetDates 파라미터 추가 (1~2개 랜덤 선택)
        if (Math.random() < 0.5 && TARGET_DATES.length > 0) {
            const count = Math.floor(Math.random() * 2) + 1;
            const selectedDates = getRandomSubarray(TARGET_DATES, count);
            selectedDates.forEach(date => queryParams.push(`targetDates=${date}`));
        }

        // 페이지네이션 파라미터 (랜덤 페이지, 고정 사이즈)
        const page = Math.floor(Math.random() * 10); // 0~9 페이지 랜덤 요청
        const size = 10;
        queryParams.push(`page=${page}`);
        queryParams.push(`size=${size}`);

        const url = `${BASE_URL}/worker-requests?${queryParams.join('&')}`;

        const res = http.get(url, { tags: { name: 'GetWorkerRequests' } });

        // 응답 시간 및 성공/실패율 기록
        getWorkerRequestsDuration.add(res.timings.duration);
        getWorkerRequestsFailRate.add(res.status !== 200);
        getWorkerRequestsSuccessRate.add(res.status === 200);

        check(res, {
            'is status 200': (r) => r.status === 200,
            'response body is not empty': (r) => r.body && r.body.length > 0,
            'response content is a slice': (r) => {
                try {
                    const json = r.json();
                    return typeof json === 'object' && json !== null && 'content' in json && 'hasNext' in json;
                } catch (e) {
                    return false;
                }
            },
        });
    });

    sleep(1); // 각 VU는 1초 대기 후 다음 요청 실행
}

// 배열에서 랜덤하게 n개의 요소를 선택하는 헬퍼 함수
function getRandomSubarray(arr, size) {
    const shuffled = arr.slice(0);
    let i = arr.length;
    const min = i - size;
    let temp, index;
    while (i-- > min) {
        index = Math.floor((i + 1) * Math.random());
        temp = shuffled[index];
        shuffled[index] = shuffled[i];
        shuffled[i] = temp;
    }
    return shuffled.slice(min);
}