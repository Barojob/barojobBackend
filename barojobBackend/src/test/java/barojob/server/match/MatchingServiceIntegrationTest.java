package barojob.server.match;

import barojob.server.domain.employer.dto.EmployerDto;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.repository.EmployerRequestRepository;
import barojob.server.domain.employer.service.EmployerRequestService;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.jobType.repository.JobTypeRepository;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import barojob.server.domain.match.dto.MatchDto;
import barojob.server.domain.match.repository.MatchRepository;
import barojob.server.domain.match.service.MatchService;
import barojob.server.domain.user.repository.UserRepository;
import barojob.server.domain.worker.dto.WorkerDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import barojob.server.domain.worker.service.WorkerRequestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder; // Security 사용 시 필요
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
@Transactional
@ActiveProfiles("test")
class MatchingServiceIntegrationTest {

    @Autowired private MatchService matchService;
    @Autowired private MatchRepository matchRepository;
    @Autowired private WorkerRequestService workerRequestService;
    @Autowired private EmployerRequestService employerRequestService;
    @Autowired private UserRepository userRepository;
    @Autowired private JobTypeRepository jobTypeRepository;
    @Autowired private NeighborhoodRepository neighborhoodRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private WorkerRequestRepository workerRequestRepository;
    @Autowired private EmployerRequestRepository employerRequestRepository;

    private List<Worker> testWorkers = new ArrayList<>();
    private List<Employer> testEmployers = new ArrayList<>();
    private List<JobType> testJobTypes = new ArrayList<>();
    private List<Neighborhood> testNeighborhoods = new ArrayList<>();
    private final LocalDate targetDate = LocalDate.of(2025, 4, 15); // 테스트 대상 날짜
    private final int requestCount = 100; // 생성할 요청 수

    @BeforeEach
    void setUp() {
        // --- 기본 데이터 생성 ---
        // JobTypes
        testJobTypes.clear();
        if (jobTypeRepository.count() == 0) { // 중복 생성 방지
            testJobTypes.add(jobTypeRepository.save(JobType.builder().name("단순노무").baseRate(BigDecimal.valueOf(10000)).build()));
            testJobTypes.add(jobTypeRepository.save(JobType.builder().name("사무보조").baseRate(BigDecimal.valueOf(12000)).build()));
            testJobTypes.add(jobTypeRepository.save(JobType.builder().name("주방보조").baseRate(BigDecimal.valueOf(11000)).build()));
            testJobTypes.add(jobTypeRepository.save(JobType.builder().name("서빙").baseRate(BigDecimal.valueOf(11500)).build()));
        } else {
            testJobTypes = jobTypeRepository.findAll();
        }


        // Neighborhoods
        testNeighborhoods.clear();
        if (neighborhoodRepository.count() == 0) { // 중복 생성 방지
            testNeighborhoods.add(neighborhoodRepository.save(Neighborhood.builder().neighborhoodName("테스트동1").build()));
            testNeighborhoods.add(neighborhoodRepository.save(Neighborhood.builder().neighborhoodName("테스트동2").build()));
            testNeighborhoods.add(neighborhoodRepository.save(Neighborhood.builder().neighborhoodName("알바동").build()));
            testNeighborhoods.add(neighborhoodRepository.save(Neighborhood.builder().neighborhoodName("구인동").build()));
        } else {
            testNeighborhoods = neighborhoodRepository.findAll();
        }


        // Workers & Employers (User 상속)
        testWorkers.clear();
        testEmployers.clear();
        if (userRepository.count() == 0) { // 중복 생성 방지
            for (int i = 0; i < requestCount / 2; i++) { // 근로자 50명 생성
                Worker worker = Worker.builder()
                        .email("worker" + i + "@test.com")
                        .nickname("근로자" + i)
                        .password(passwordEncoder.encode("password")) // 비밀번호 암호화
                        .name("김근로" + i)
                        .phoneNumber("010-1111-" + String.format("%04d", i))
                        .priorityScore(30.0 + (i % 70)) // 30 ~ 99.x 점수 분포
                        .build();
                testWorkers.add(userRepository.save(worker));
            }
            for (int i = 0; i < requestCount / 2; i++) { // 고용주 50명 생성
                Employer employer = Employer.builder()
                        .email("employer" + i + "@test.com")
                        .nickname("고용주" + i)
                        .password(passwordEncoder.encode("password"))
                        .businessName("바로잡컴퍼니" + i)
                        .name("박사장" + i)
                        .phoneNumber("010-2222-" + String.format("%04d", i))
                        .build();
                testEmployers.add(userRepository.save(employer));
            }
        } else {
            userRepository.findAll().forEach(user -> {
                if (user instanceof Worker) testWorkers.add((Worker) user);
                if (user instanceof Employer) testEmployers.add((Employer) user);
            });
        }

        // --- 요청 데이터 생성 (WorkerRequest & EmployerRequest) ---
        Random random = new Random();
        // Worker Requests 생성
        IntStream.range(0, requestCount).forEach(i -> {
            Worker worker = testWorkers.get(i % testWorkers.size());
            List<Long> neighborhoodIds = new ArrayList<>();
            neighborhoodIds.add(testNeighborhoods.get(random.nextInt(testNeighborhoods.size())).getNeighborhoodId());
            if (random.nextBoolean()) { // 랜덤하게 지역 1~2개 추가
                neighborhoodIds.add(testNeighborhoods.get(random.nextInt(testNeighborhoods.size())).getNeighborhoodId());
            }

            List<Long> jobTypeIds = new ArrayList<>();
            jobTypeIds.add(testJobTypes.get(random.nextInt(testJobTypes.size())).getJobTypeId());
            if (random.nextBoolean()) { // 랜덤하게 직종 1~2개 추가
                jobTypeIds.add(testJobTypes.get(random.nextInt(testJobTypes.size())).getJobTypeId());
            }

            WorkerDto.CreateRequest workerDto = WorkerDto.CreateRequest.builder()
                    .workerId(worker.getId())
                    .requestDate(targetDate) // 테스트 대상 날짜로 설정
                    .neighborhoodIds(neighborhoodIds.stream().distinct().toList())
                    .jobTypeIds(jobTypeIds.stream().distinct().toList())
                    .build();
            // 이미 생성된 요청이 있다면 건너뛰기 (테스트 반복 실행 고려)
            if (!workerRequestRepository.existsByWorkerAndRequestDate(worker, targetDate)) {
                workerRequestService.createWorkerRequest(workerDto);
            }
        });

        // Employer Requests 생성
        IntStream.range(0, requestCount).forEach(i -> {
            Employer employer = testEmployers.get(i % testEmployers.size());
            Neighborhood location = testNeighborhoods.get(random.nextInt(testNeighborhoods.size()));
            JobType jobType = testJobTypes.get(random.nextInt(testJobTypes.size()));

            EmployerDto.CreateDetail detail = EmployerDto.CreateDetail.builder()
                    .jobTypeId(jobType.getJobTypeId())
                    .requiredCount(random.nextInt(1, 4)) // 1~3명 랜덤 요청
                    .build();

            EmployerDto.CreateRequest employerDto = EmployerDto.CreateRequest.builder()
                    .employerId(employer.getId())
                    .requestDate(targetDate) // 테스트 대상 날짜로 설정
                    .locationNeighborhoodId(location.getNeighborhoodId())
                    .details(List.of(detail))
                    .build();
            // EmployerRequest는 중복 생성 허용한다고 가정 (동일 고용주, 날짜, 장소라도 다른 직종/인원 요청 가능)
            employerRequestService.createEmployerRequest(employerDto);
        });

        log.info("테스트 데이터 생성 완료: Worker Requests={}, Employer Requests={}",
                workerRequestRepository.count(), employerRequestRepository.count());
    }

    @Test
    @DisplayName("매칭 로직 통합 테스트 - 특정 날짜에 대해 매칭 실행 및 결과 확인")
    void performMatching_IntegrationTest() {
        // Arrange (데이터는 @BeforeEach 에서 생성됨)
        long initialMatchCount = matchRepository.count();

        // Act
        MatchDto.Response response = matchService.performMatching(targetDate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTargetDate()).isEqualTo(targetDate);

        // 매칭이 최소 1건 이상 발생했는지 확인 (테스트 데이터에 따라 기대값 조절)
        // 정확한 매칭 건수 예측은 어려우므로, 0보다 큰지만 확인하거나,
        // 특정 매칭이 반드시 일어나도록 데이터를 설계하고 검증할 수 있음
        assertThat(response.getTotalMatchesMade()).isGreaterThan(0);
        assertThat(response.getMatches()).isNotEmpty();
        assertThat(response.getMatches().size()).isEqualTo(response.getTotalMatchesMade());

        log.info("매칭 결과: 총 {} 건 매칭됨", response.getTotalMatchesMade());
        response.getMatches().forEach(matchInfo -> {
            log.debug("  매칭 상세: Worker ID={}, Employer ID={}, Job Type={}, Neighborhood={}",
                    matchInfo.getWorkerId(), matchInfo.getEmployerId(), matchInfo.getJobTypeName(), matchInfo.getNeighborhoodName());
            assertThat(matchInfo.getMatchId()).isNotNull();
            assertThat(matchInfo.getWorkerId()).isNotNull();
            assertThat(matchInfo.getEmployerId()).isNotNull();
            assertThat(matchInfo.getMatchDateTime()).isNotNull();
        });
    }
}