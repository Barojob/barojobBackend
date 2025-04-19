package barojob.server.match;

import barojob.server.domain.match.dto.MatchingDto;
import barojob.server.domain.match.repository.MatchRepository; // 결과 확인용
import barojob.server.domain.employer.repository.EmployerRequestRepository; // 결과 확인용
import barojob.server.domain.match.service.TestMatchService;
import barojob.server.domain.worker.repository.WorkerRequestRepository; // 결과 확인용
import barojob.server.common.type.RequestStatus; // 상태 확인용

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;

@SpringBootTest
@Transactional
@Commit
class TestMatchServiceIntegrationTest {

    @Autowired
    private TestMatchService testMatchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private WorkerRequestRepository workerRequestRepository;

    @Autowired
    private EmployerRequestRepository employerRequestRepository;

    @Test
    @DisplayName("테스트 데이터 생성 및 매칭 실행 통합 테스트 (DB 커밋)")
    void runTestMatching_IntegrationTest_WithCommit() {
        // given: 테스트 실행 전 매칭 수 기록
        long initialMatchCount = matchRepository.count();
        LocalDate expectedRequestDate = LocalDate.now().plusDays(1);

        // when: 테스트 서비스의 핵심 로직 실행
        MatchingDto.Response response = testMatchService.runTestMatching();

        System.out.println("테스트 매칭 결과 메시지: " + response.getMessage());
        System.out.println("총 매칭 건수: " + response.getTotalMatchesMade());
    }
}