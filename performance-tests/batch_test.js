import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 테스트 설정
export const options = {
    scenarios: {
        single_batch_run: {
            executor: 'per-vu-iterations', // 각 VU가 지정된 반복 횟수만큼 실행
            vus: 1,                       // 동시 사용자 수 (너무 높게 잡으면 DB 부하 심할 수 있음)
            iterations: 1,                // 각 VU당 반복 횟수
            maxDuration: '5m',           // 최대 테스트 실행 시간
        },
    },
    thresholds: {
        'http_req_duration{scenario:single_batch_run}': ['p(95)<300000'], // 5분
        'http_req_failed{scenario:single_batch_run}': ['rate<0.05'],    // 실패율 5% 미만
        'checks{scenario:single_batch_run}': ['rate>0.95'],
    }
};

// API 기본 URL
const BASE_URL = 'http://localhost:8080'; // 실제 애플리케이션 주소로 변경하세요.

// 사용자 정의 메트릭
const runMatchingDuration = new Trend('run_matching_duration');
const runMatchingFailRate = new Rate('run_matching_fail_rate');
const runMatchingSuccessRate = new Rate('run_matching_success_rate');

export default function () {
    group('RunMatchingAPI', function () {
        const url = `${BASE_URL}/api/match/run`;

        // POST 요청 (헤더나 바디는 현재 엔드포인트 정의에 없음)
        const res = http.post(url, null, { tags: { name: 'RunMatching' } });

        runMatchingDuration.add(res.timings.duration);
        runMatchingFailRate.add(res.status !== 200);
        runMatchingSuccessRate.add(res.status === 200);

        check(res, {
            'is status 200': (r) => r.status === 200,
            'response has targetDate': (r) => {
                try {
                    const json = r.json();
                    return typeof json === 'object' && json !== null && 'targetDate' in json;
                } catch (e) {
                    return false;
                }
            },
            'response has totalMatchesMade': (r) => {
                try {
                    const json = r.json();
                    return typeof json === 'object' && json !== null && 'totalMatchesMade' in json;
                } catch (e) {
                    return false;
                }
            },
        });
    });

    sleep(5); // 다음 반복 전 5초 대기
}