package barojob.server.domain.match.service;

import barojob.server.domain.employer.dto.EmployerDto;
import barojob.server.domain.employer.dto.EmployerRequestDto;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.repository.EmployerRepository;
import barojob.server.domain.employer.service.EmployerRequestService;
import barojob.server.domain.employer.service.EmployerService;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.jobType.repository.JobTypeRepository;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import barojob.server.domain.match.dto.MatchingDto;
import barojob.server.domain.user.repository.UserRepository;
import barojob.server.domain.worker.dto.WorkerDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.repository.WorkerRepository;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import barojob.server.domain.worker.service.WorkerRequestService;
import barojob.server.domain.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // 추가

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*; // 추가

@Slf4j
@Service
@RequiredArgsConstructor
public class TestMatchService {

    private final WorkerService workerService;
    private final EmployerService employerService;
    private final WorkerRequestService workerRequestService;
    private final EmployerRequestService employerRequestService;
    private final MatchService matchService;

    private final WorkerRepository workerRepository;
    private final EmployerRepository employerRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final JobTypeRepository jobTypeRepository;
    private final UserRepository userRepository;
    private final WorkerRequestRepository workerRequestRepository;

    private List<Long> availableNeighborhoodIds = new ArrayList<>();
    private List<Long> availableJobTypeIds = new ArrayList<>();
    private final Random random = new Random();

    // 샘플 데이터 (기존과 동일)
    private final Map<String, String> sampleNeighborhoods = Map.of(
            "강남구 역삼동", "강남구", "서초구 서초동", "서초구",
            "송파구 잠실동", "송파구", "마포구 서교동", "마포구",
            "용산구 이태원동", "용산구", "종로구 인사동", "종로구",
            "영등포구 여의도동", "영등포구"
    );
    private final Map<String, BigDecimal> sampleJobTypes = Map.of(
            "서빙", BigDecimal.valueOf(11000), "주방보조", BigDecimal.valueOf(12000),
            "카페알바", BigDecimal.valueOf(10500), "편의점", BigDecimal.valueOf(10000),
            "단순노무", BigDecimal.valueOf(13000), "배달", BigDecimal.valueOf(15000),
            "사무보조", BigDecimal.valueOf(11500)
    );

    // 기초 데이터 확보 로직 (기존과 동일)
    private void ensureBaseDataExists() {
        log.info("기초 데이터 확인 및 생성 시작...");
        availableNeighborhoodIds.clear();
        availableJobTypeIds.clear();

        // Neighborhood 데이터 확인 및 생성
        sampleNeighborhoods.forEach((neighborhoodName, districtName) -> {
            Neighborhood neighborhood = neighborhoodRepository.findByNeighborhoodName(neighborhoodName)
                    .orElseGet(() -> {
                        log.info("Neighborhood 생성: {}", neighborhoodName);
                        return neighborhoodRepository.save(Neighborhood.builder()
                                .neighborhoodName(neighborhoodName)
                                .build());
                    });
            availableNeighborhoodIds.add(neighborhood.getNeighborhoodId());
        });

        // JobType 데이터 확인 및 생성
        sampleJobTypes.forEach((name, baseRate) -> {
            JobType jobType = jobTypeRepository.findByName(name)
                    .orElseGet(() -> {
                        log.info("JobType 생성: {}, 기본 시급: {}", name, baseRate);
                        return jobTypeRepository.save(JobType.builder()
                                .name(name)
                                .baseRate(baseRate)
                                .build());
                    });
            availableJobTypeIds.add(jobType.getJobTypeId());
        });

        if (availableNeighborhoodIds.isEmpty()) {
            log.error("사용 가능한 Neighborhood 데이터가 없습니다. 샘플 데이터 또는 DB 확인 필요.");
            // 예외를 던지거나 기본값 설정 등의 처리 추가 가능
        }
        if (availableJobTypeIds.isEmpty()) {
            log.error("사용 가능한 JobType 데이터가 없습니다. 샘플 데이터 또는 DB 확인 필요.");
            // 예외를 던지거나 기본값 설정 등의 처리 추가 가능
        }

        log.info("기초 데이터 확인 및 생성 완료. 사용 가능 동네 ID: {}, 사용 가능 직종 ID: {}", availableNeighborhoodIds, availableJobTypeIds);
    }
    /**
     * 테스트용 근로자, 업주 및 요청 데이터를 생성하고 배치 매칭을 실행하는 메소드
     * @param workerCount 생성할 근로자 수
     * @param employerCount 생성할 업주 수
     * @param workerRequestRatio 근로자 중 요청을 생성할 비율 (0.0 ~ 1.0)
     * @param employerRequestRatio 업주 중 요청을 생성할 비율 (0.0 ~ 1.0)
     * @return 매칭 결과 DTO
     */
    @Transactional
    public MatchingDto.Response runFullTestMatching(int workerCount, int employerCount, double workerRequestRatio, double employerRequestRatio) { //
        log.info("테스트 매칭 데이터 생성 및 매칭 실행 시작 (근로자: {}, 업주: {}, 근로자요청비율: {}, 업주요청비율: {})",
                workerCount, employerCount, workerRequestRatio, employerRequestRatio);

        ensureBaseDataExists(); //

        // 기초 데이터 없으면 에러 반환
        if (availableNeighborhoodIds.isEmpty() || availableJobTypeIds.isEmpty()) {
            log.error("기초 데이터(동네 또는 직종)가 없어 테스트 매칭을 정상적으로 진행할 수 없습니다.");
            return MatchingDto.Response.builder() //
                    .targetDate(LocalDate.now().plusDays(1)) //
                    .totalMatchesMade(0) //
                    .matches(List.of()) //
                    .message("오류: 매칭에 필요한 기초 데이터(동네, 직종)가 없습니다.") //
                    .build();
        }

        // 테스트용 근로자/업주 생성
        List<Worker> workers = createTestWorkers(workerCount); //
        List<Employer> employers = createTestEmployers(employerCount); //

        // 테스트용 요청 생성
        createTestWorkerRequests(workers, workerRequestRatio); //
        createTestEmployerRequests(employers, employerRequestRatio); //

        // 매칭 실행
        LocalDateTime executionTime = LocalDateTime.now();
        MatchingDto.Response matchingResult = matchService.performDailyBatchMatching(executionTime); //

        log.info("테스트 매칭 완료. 총 {}건 매칭됨.", matchingResult.getTotalMatchesMade()); //
        return matchingResult; //
    }

    // 테스트 근로자 생성
    private List<Worker> createTestWorkers(int count) { //
        List<Worker> createdWorkers = new ArrayList<>();
        log.info("테스트 근로자 {}명 생성 시도 시작...", count);
        for (int i = 1; i <= count; i++) {
            long uniqueSuffix = System.nanoTime() ^ random.nextLong(); // 고유성 강화
            String email = "worker" + uniqueSuffix + "@test.com";
            String phone = "010-" + String.format("%04d", Math.abs(random.nextInt(10000))) + String.format("%04d", i);
            String nickname = "테스트근로자" + Math.abs(uniqueSuffix % 100000);

            // 중복 체크 강화
            if (userRepository.existsByEmail(email) || userRepository.findByNickname(nickname).isPresent() || workerRepository.existsByPhoneNumber(phone)) { //
                log.warn("Skipping worker creation due to existing data: email={}, nickname={}, phone={}", email, nickname, phone);
                continue;
            }

            WorkerDto.CreateRequest request = WorkerDto.CreateRequest.builder() //
                    .email(email) //
                    .nickname(nickname) //
                    .password("password") //
                    .name("근로자" + i) //
                    .phoneNumber(phone) //
                    .priorityScore(50.0 + (random.nextDouble() * 50)) // 50 ~ 100 사이 점수 //
                    .build();
            try {
                WorkerDto.CreateResponse response = workerService.createWorker(request); //
                workerRepository.findById(response.getUserId()).ifPresent(createdWorkers::add); //
            } catch (Exception e) {
                log.warn("근로자 생성 중 오류 발생 (요청: {}): {}", request.getEmail(), e.getMessage()); //
            }
        }
        log.info("테스트 근로자 {}명 생성 완료. 실제 생성된 수: {}", count, createdWorkers.size());
        return createdWorkers;
    }

    // 테스트 업주 생성
    private List<Employer> createTestEmployers(int count) { //
        List<Employer> createdEmployers = new ArrayList<>();
        log.info("테스트 업주 {}명 생성 시도 시작...", count);
        for (int i = 1; i <= count; i++) {
            long uniqueSuffix = System.nanoTime() ^ random.nextLong(); // 고유성 강화
            String email = "employer" + uniqueSuffix + "@test.com";
            String phone = "011-" + String.format("%04d", Math.abs(random.nextInt(10000))) + String.format("%04d", i);
            String nickname = "테스트업주" + Math.abs(uniqueSuffix % 100000);

            // 중복 체크 강화
            if (userRepository.existsByEmail(email) || userRepository.findByNickname(nickname).isPresent() || employerRepository.existsByPhoneNumber(phone)) { //
                log.warn("Skipping employer creation due to existing data: email={}, nickname={}, phone={}", email, nickname, phone);
                continue;
            }

            EmployerDto.CreateRequest request = EmployerDto.CreateRequest.builder() //
                    .email(email) //
                    .nickname(nickname) //
                    .password("password") //
                    .businessName("테스트사업장" + i) //
                    .name("업주" + i) //
                    .phoneNumber(phone) //
                    .build();
            try {
                EmployerDto.CreateResponse response = employerService.createEmployer(request); //
                employerRepository.findById(response.getUserId()).ifPresent(createdEmployers::add); //
            } catch (Exception e) {
                log.warn("업주 생성 중 오류 발생 (요청: {}): {}", request.getEmail(), e.getMessage()); //
            }
        }
        log.info("테스트 업주 {}명 생성 완료. 실제 생성된 수: {}", count, createdEmployers.size());
        return createdEmployers;
    }

    // 테스트 근로자 요청 생성
    private void createTestWorkerRequests(List<Worker> workers, double ratio) { //
        if (CollectionUtils.isEmpty(workers) || availableNeighborhoodIds.isEmpty() || availableJobTypeIds.isEmpty()) {
            log.warn("근로자 요청 생성 불가: 입력 데이터 부족");
            return;
        }
        LocalDate requestTargetDate = LocalDate.now().plusDays(1);
        int targetCount = (int) (workers.size() * ratio);
        int createdCount = 0;
        Collections.shuffle(workers); // 무작위 근로자 선택을 위해 섞음

        log.info("테스트 근로자 요청 {}건 생성 시도 시작...", targetCount);
        for (int i = 0; i < targetCount && i < workers.size(); i++) {
            Worker worker = workers.get(i);

            // 해당 날짜에 이미 요청이 있는지 확인 (WorkerRequestService 내부에 동일 로직 있음)
            if (workerRequestRepository.existsByWorkerAndRequestDate(worker, requestTargetDate)) { //
                log.trace("Skipping worker request creation for worker {} on date {}: already exists", worker.getId(), requestTargetDate); //
                continue;
            }

            // 랜덤하게 희망 동네 1~3개 선택
            List<Long> selectedNIds = getRandomSublist(availableNeighborhoodIds, random.nextInt(3) + 1);
            // 랜덤하게 가능 업종 1~3개 선택
            List<Long> selectedJobIds = getRandomSublist(availableJobTypeIds, random.nextInt(3) + 1);

            if (selectedNIds.isEmpty() || selectedJobIds.isEmpty()) {
                log.warn("Skipping worker request creation for worker {}: could not select neighborhoods or job types", worker.getId()); //
                continue;
            }

            WorkerRequestDto.CreateRequest workerRequest = WorkerRequestDto.CreateRequest.builder() //
                    .workerId(worker.getId()) //
                    .requestDate(requestTargetDate) //
                    .neighborhoodIds(selectedNIds) //
                    .jobTypeIds(selectedJobIds) //
                    .build();
            try {
                workerRequestService.createWorkerRequest(workerRequest); //
                createdCount++;
            } catch (Exception e) {
                // 이미 존재하는 요청(IllegalStateException) 외의 오류만 로깅
                if (!(e instanceof IllegalStateException)) {
                    log.error("Error creating worker request for worker ID {}: {}", worker.getId(), e.getMessage()); //
                } else {
                    log.trace("Worker request already exists for worker {} on date {}", worker.getId(), requestTargetDate); //
                }
            }
        }
        log.info("테스트 근로자 요청 {}건 생성 완료. 실제 생성 시도된 수: {}", targetCount, createdCount);
    }

    // 테스트 업주 요청 생성
    private void createTestEmployerRequests(List<Employer> employers, double ratio) { //
        if (CollectionUtils.isEmpty(employers) || availableNeighborhoodIds.isEmpty() || availableJobTypeIds.isEmpty()) {
            log.warn("업주 요청 생성 불가: 입력 데이터 부족");
            return;
        }
        LocalDate requestTargetDate = LocalDate.now().plusDays(1);
        int targetCount = (int) (employers.size() * ratio);
        int createdCount = 0;
        Collections.shuffle(employers);

        log.info("테스트 업주 요청 {}건 생성 시도 시작...", targetCount);
        for (int i = 0; i < targetCount && i < employers.size(); i++) {
            Employer employer = employers.get(i);
            // 랜덤하게 근무지 동네 1개 선택
            Long selectedNId = availableNeighborhoodIds.get(random.nextInt(availableNeighborhoodIds.size()));

            // 랜덤하게 1~2개의 상세 요청 생성
            int detailCount = random.nextInt(2) + 1;
            List<EmployerRequestDto.CreateDetail> details = new ArrayList<>();
            // 사용 가능한 직종 ID 복사본 생성 (중복 선택 방지)
            List<Long> jobPool = new ArrayList<>(availableJobTypeIds);
            Collections.shuffle(jobPool);

            for (int j = 0; j < detailCount && !jobPool.isEmpty(); j++) {
                // 랜덤하게 가능 업종 1개 선택 및 pool에서 제거
                Long selectedJobId = jobPool.remove(0);
                // 랜덤하게 필요 인원 1~3명 설정
                int required = random.nextInt(3) + 1;

                details.add(EmployerRequestDto.CreateDetail.builder() //
                        .jobTypeId(selectedJobId) //
                        .requiredCount(required) //
                        .build());
            }

            if (details.isEmpty()) {
                log.warn("Skipping employer request creation for employer {}: could not create details", employer.getId()); //
                continue;
            }

            EmployerRequestDto.CreateRequest employerRequest = EmployerRequestDto.CreateRequest.builder() //
                    .employerId(employer.getId()) //
                    .requestDate(requestTargetDate) //
                    .locationNeighborhoodId(selectedNId) //
                    .details(details) //
                    .build();
            try {
                employerRequestService.createEmployerRequest(employerRequest); //
                createdCount++;
            } catch (Exception e) {
                log.error("Error creating employer request for employer ID {}: {}", employer.getId(), e.getMessage()); //
            }
        }
        log.info("테스트 업주 요청 {}건 생성 완료. 실제 생성 시도된 수: {}", targetCount, createdCount);
    }

    // 리스트에서 랜덤하게 n개의 부분 리스트를 추출하는 헬퍼 메소드
    private <T> List<T> getRandomSublist(List<T> list, int n) { //
        if (list == null || list.isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        List<T> mutableList = new ArrayList<>(list);
        Collections.shuffle(mutableList, random); // 원본 리스트 대신 복사본을 섞음
        return mutableList.subList(0, Math.min(n, mutableList.size()));
    }
}