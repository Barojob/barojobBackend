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
import java.util.stream.Collectors; // 추가

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

    @Transactional
    public MatchingDto.Response runTestMatching() {
        log.info("테스트 매칭 데이터 생성 및 매칭 실행 시작");

        ensureBaseDataExists(); // 기초 데이터 확보

        // 기초 데이터 없으면 에러 반환 (기존과 동일)
        if (availableNeighborhoodIds.isEmpty() || availableJobTypeIds.isEmpty()) {
            log.error("기초 데이터(동네 또는 직종)가 없어 테스트 매칭을 정상적으로 진행할 수 없습니다.");
            return MatchingDto.Response.builder()
                    .targetDate(LocalDate.now().plusDays(1))
                    .totalMatchesMade(0)
                    .matches(List.of())
                    .message("오류: 매칭에 필요한 기초 데이터(동네, 직종)가 없습니다.")
                    .build();
        }

        // 테스트용 근로자/업주 생성 (기존과 동일)
        List<Worker> workers = createTestWorkers(20);
        List<Employer> employers = createTestEmployers(20);

        createGuaranteedMatchingRequests(workers, employers);

        // 매칭 실행 (기존과 동일)
        LocalDateTime executionTime = LocalDateTime.now();
        MatchingDto.Response matchingResult = matchService.performDailyBatchMatching(executionTime);

        log.info("테스트 매칭 완료. 총 {}건 매칭됨.", matchingResult.getTotalMatchesMade());
        return matchingResult;
    }

    // 근로자 생성 (기존과 동일)
    private List<Worker> createTestWorkers(int count) {
        List<Worker> createdWorkers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            long uniqueSuffix = System.nanoTime();
            String email = "worker" + uniqueSuffix + "_" + uniqueSuffix % 10000 + "@test.com";
            String phone = "010-" + String.valueOf(uniqueSuffix % 10000) + String.format("%04d", i);
            String nickname = "테스트근로자" + String.valueOf(uniqueSuffix % 10000);

            if (userRepository.existsByEmail(email) || userRepository.findByNickname(nickname).isPresent() || workerRepository.existsByPhoneNumber(phone)) {
                log.warn("Skipping worker creation due to existing email/nickname/phone: email={}, nickname={}, phone={}", email, nickname, phone);
                continue;
            }

            WorkerDto.CreateRequest request = WorkerDto.CreateRequest.builder()
                    .email(email)
                    .nickname(nickname)
                    .password("password")
                    .name("근로자" + i)
                    .phoneNumber(phone)
                    .priorityScore(50.0 + (Math.random() * 50)) // 50 ~ 100 사이 점수
                    .build();
            try {
                WorkerDto.CreateResponse response = workerService.createWorker(request);
                workerRepository.findById(response.getUserId()).ifPresent(createdWorkers::add);
            } catch (Exception e) { // 중복 포함 모든 예외 처리
                log.warn("근로자 생성 중 오류 발생 (요청: {}): {}", request.getEmail(), e.getMessage());
            }
        }
        log.info("테스트 근로자 {}명 생성 시도 완료.", count);
        return createdWorkers;
    }

    // 업주 생성 (기존과 동일, 중복 체크 강화)
    private List<Employer> createTestEmployers(int count) {
        List<Employer> createdEmployers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            long uniqueSuffix = System.nanoTime();
            String email = "employer" + uniqueSuffix + "_" + uniqueSuffix % 10000 + "@test.com";
            String phone = "011-" + String.valueOf(uniqueSuffix % 10000) + String.format("%04d", i);
            String nickname = "테스트업주" + String.valueOf(uniqueSuffix % 10000);

            // 이메일/닉네임/전화번호 중복 체크 강화
            if (userRepository.existsByEmail(email) || userRepository.findByNickname(nickname).isPresent() || employerRepository.existsByPhoneNumber(phone)) {
                log.warn("Skipping employer creation due to existing email/nickname/phone: email={}, nickname={}, phone={}", email, nickname, phone);
                continue;
            }

            EmployerDto.CreateRequest request = EmployerDto.CreateRequest.builder()
                    .email(email)
                    .nickname(nickname)
                    .password("password")
                    .businessName("테스트사업장" + i)
                    .name("업주" + i)
                    .phoneNumber(phone)
                    .build();
            try {
                EmployerDto.CreateResponse response = employerService.createEmployer(request);
                employerRepository.findById(response.getUserId()).ifPresent(createdEmployers::add);
            } catch (Exception e) { // 중복 포함 모든 예외 처리
                log.warn("업주 생성 중 오류 발생 (요청: {}): {}", request.getEmail(), e.getMessage());
            }
        }
        log.info("테스트 업주 {}명 생성 시도 완료.", count);
        return createdEmployers;
    }


    /**
     * 매칭이 반드시 일어나도록 요청 데이터를 생성하는 메소드
     */
    private void createGuaranteedMatchingRequests(List<Worker> workers, List<Employer> employers) {
        if (CollectionUtils.isEmpty(workers) || CollectionUtils.isEmpty(employers)) {
            log.warn("매칭 보장 요청 생성 불가: 근로자 또는 업주 리스트가 비어있음.");
            return;
        }
        if (availableNeighborhoodIds.isEmpty() || availableJobTypeIds.isEmpty()) {
            log.warn("매칭 보장 요청 생성 불가: 사용 가능한 동네 또는 직종 ID가 없음.");
            return;
        }

        LocalDate requestTargetDate = LocalDate.now().plusDays(1);
        Random random = new Random();
        int createdWorkerReqCount = 0;
        int createdEmployerReqCount = 0;

        // --- 시나리오 1: 역삼동(ID 1)에서 서빙(ID 10) 매칭 ---
        Long targetNId1 = findAvailableId(availableNeighborhoodIds, 0); // 첫번째 동네 ID
        Long targetJobId1 = findAvailableId(availableJobTypeIds, 0);   // 첫번째 직종 ID

        if (targetNId1 != null && targetJobId1 != null) {
            // 근로자 요청 생성 (최대 5명)
            for (int i = 0; i < Math.min(5, workers.size()); i++) {
                Worker worker = workers.get(i);
                if (workerRequestRepository.existsByWorkerAndRequestDate(worker, requestTargetDate)) continue;

                WorkerRequestDto.CreateRequest workerRequest = WorkerRequestDto.CreateRequest.builder()
                        .workerId(worker.getId())
                        .requestDate(requestTargetDate)
                        .neighborhoodIds(List.of(targetNId1)) // 역삼동만 희망
                        .jobTypeIds(List.of(targetJobId1))      // 서빙만 가능
                        .build();
                try {
                    workerRequestService.createWorkerRequest(workerRequest);
                    createdWorkerReqCount++;
                } catch (Exception e) { log.error("Error creating guaranteed worker request 1", e); }
            }
            // 업주 요청 생성 (최대 2곳, 각 1~2명씩 필요)
            for (int i = 0; i < Math.min(2, employers.size()); i++) {
                Employer employer = employers.get(i);
                EmployerRequestDto.CreateRequest employerRequest = EmployerRequestDto.CreateRequest.builder()
                        .employerId(employer.getId())
                        .requestDate(requestTargetDate)
                        .locationNeighborhoodId(targetNId1) // 역삼동에서 구함
                        .details(List.of(
                                EmployerRequestDto.CreateDetail.builder()
                                        .jobTypeId(targetJobId1) // 서빙 구함
                                        .requiredCount(random.nextInt(2) + 1) // 1~2명
                                        .build()
                        ))
                        .build();
                try {
                    employerRequestService.createEmployerRequest(employerRequest);
                    createdEmployerReqCount++;
                } catch (Exception e) { log.error("Error creating guaranteed employer request 1", e); }
            }
        }

        // --- 시나리오 2: 서초동(ID 2)에서 주방보조(ID 11) 매칭 ---
        Long targetNId2 = findAvailableId(availableNeighborhoodIds, 1); // 두번째 동네 ID
        Long targetJobId2 = findAvailableId(availableJobTypeIds, 1);   // 두번째 직종 ID

        if (targetNId2 != null && targetJobId2 != null && workers.size() >= 10 && employers.size() >= 4) { // 충분한 유저가 있을 때만
            // 근로자 요청 생성 (5명 ~ 9번 근로자)
            for (int i = 5; i < 10; i++) {
                Worker worker = workers.get(i);
                if (workerRequestRepository.existsByWorkerAndRequestDate(worker, requestTargetDate)) continue;

                WorkerRequestDto.CreateRequest workerRequest = WorkerRequestDto.CreateRequest.builder()
                        .workerId(worker.getId())
                        .requestDate(requestTargetDate)
                        .neighborhoodIds(List.of(targetNId2)) // 서초동만 희망
                        .jobTypeIds(List.of(targetJobId2))      // 주방보조만 가능
                        .build();
                try {
                    workerRequestService.createWorkerRequest(workerRequest);
                    createdWorkerReqCount++;
                } catch (Exception e) { log.error("Error creating guaranteed worker request 2", e); }
            }
            // 업주 요청 생성 (2곳 ~ 3번 업주, 각 1명씩 필요)
            for (int i = 2; i < 4; i++) {
                Employer employer = employers.get(i);
                EmployerRequestDto.CreateRequest employerRequest = EmployerRequestDto.CreateRequest.builder()
                        .employerId(employer.getId())
                        .requestDate(requestTargetDate)
                        .locationNeighborhoodId(targetNId2) // 서초동에서 구함
                        .details(List.of(
                                EmployerRequestDto.CreateDetail.builder()
                                        .jobTypeId(targetJobId2) // 주방보조 구함
                                        .requiredCount(1)        // 1명
                                        .build()
                        ))
                        .build();
                try {
                    employerRequestService.createEmployerRequest(employerRequest);
                    createdEmployerReqCount++;
                } catch (Exception e) { log.error("Error creating guaranteed employer request 2", e); }
            }
        }

        log.info("매칭 보장 근로자 요청 {}건 / 업주 요청 {}건 생성 시도 완료 (대상 날짜: {})", createdWorkerReqCount, createdEmployerReqCount, requestTargetDate);
    }

    // 사용 가능한 ID 리스트에서 특정 인덱스의 ID를 안전하게 가져오는 헬퍼 메소드
    private Long findAvailableId(List<Long> idList, int index) {
        if (idList != null && index >= 0 && index < idList.size()) {
            return idList.get(index);
        }
        return null;
    }


    // (getRandomSublist 메소드는 기존 코드 유지)
    private <T> List<T> getRandomSublist(List<T> list, int n) {
        if (list == null || list.isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        List<T> mutableList = new ArrayList<>(list);
        Random random = new Random();
        int size = mutableList.size();
        n = Math.min(n, size);

        List<T> sublist = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // 리스트가 비어있지 않은 경우에만 remove 호출
            if (!mutableList.isEmpty()) {
                int randomIndex = random.nextInt(mutableList.size());
                sublist.add(mutableList.remove(randomIndex));
            } else {
                break; // 더 이상 뽑을 요소가 없으면 종료
            }
        }
        return sublist;
    }
}