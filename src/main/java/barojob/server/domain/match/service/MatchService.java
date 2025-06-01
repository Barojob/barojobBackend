package barojob.server.domain.match.service;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.employer.entity.EmployerRequestDetail;
import barojob.server.domain.employer.repository.EmployerRequestDetailRepository;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.match.dto.MatchingDto;
import barojob.server.domain.match.entity.Match;
import barojob.server.domain.match.repository.MatchRepository;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.entity.WorkerRequestId;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static barojob.server.domain.employer.entity.QEmployerRequest.employerRequest;
import static barojob.server.domain.employer.entity.QEmployerRequestDetail.employerRequestDetail;
import static barojob.server.domain.worker.entity.QWorkerRequest.workerRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    private final WorkerRequestRepository workerRequestRepository;
    private final EmployerRequestDetailRepository employerRequestDetailRepository;
    private final MatchRepository matchRepository;
    private final NeighborhoodRepository neighborhoodRepository; // Neighborhood 조회 위해 주입
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MatchingDto.Response performDailyBatchMatching(LocalDateTime executionTime) {

        // 1. 대상 날짜 설정
        LocalDate targetDate = executionTime.toLocalDate().plusDays(1);
        log.info("배치 매칭 프로세스 시작 - 대상 날짜: {}", targetDate);

        // todo: 2. 성능 개선 아이디어 (기존과 동일)

        // 3. 매칭 가능한 업주 요청 상세 정보 조회 (변경 없음)
        List<MatchingDataDto.EmployerDetailInfo> eligibleDetails = employerRequestDetailRepository.findEligibleDetailInfoForMatching(targetDate); //
        log.debug("매칭 가능 업주 요청 상세 정보 수: {}", eligibleDetails.size());
        if (eligibleDetails.isEmpty()) {
            return createEmptyResponse(targetDate, "매칭 대상 업주 요청이 없습니다.");
        }

        // 4. 매칭 가능한 근로자 정보 조회
        List<MatchingDataDto.WorkerInfo> eligibleWorkers = workerRequestRepository.findEligibleWorkerInfoForMatching(targetDate); //
        log.debug("매칭 가능 근로자 정보 수: {}", eligibleWorkers.size());
        if (eligibleWorkers.isEmpty()) {
            return createEmptyResponse(targetDate, "매칭 대상 근로자 요청이 없습니다.");
        }

        // 5. 잠재적 매칭 후보 생성 (변경 없음 - WorkerInfo DTO 의존)
        List<PotentialMatch> potentialMatches = generatePotentialMatches(eligibleDetails, eligibleWorkers); //

        // 6. 업주 요청 상세별 근로자 잠정 선택 (TentativeAssignment 에 neighborhoodId 추가 필요)
        List<TentativeAssignment> tentativeAssignments = selectTopWorkersForEachDetail(potentialMatches); //

        // 7. 근로자별 중복 할당 해결 (FinalMatch 에 neighborhoodId 추가 필요)
        List<FinalMatch> finalSelections = resolveWorkerConflicts(tentativeAssignments);

        // --- 최종 매칭 결과 저장 및 상태 업데이트 ---

        // 8. 최종 선택된 정보로 Match 엔티티 생성 및 저장 (WorkerRequest 참조 방식 변경, Match 에 neighborhoodId 설정)
        List<Match> createdMatches = createAndSaveMatches(finalSelections, executionTime); //

        if (CollectionUtils.isEmpty(createdMatches)) {
            log.info("새로 생성된 매칭이 없습니다.");
            return createEmptyResponse(targetDate, "매칭된 결과가 없습니다.");
        }

        // 9. 상태 업데이트 (WorkerRequest 상태 업데이트 시 복합키 사용)
        updateStatusesAfterMatching(createdMatches); //

        // 10. 결과 DTO 생성 및 반환 (mapMatchToMatchInfo 수정 필요)
        List<MatchingDto.MatchInfo> matchInfos = createdMatches.stream()
                .map(this::mapMatchToMatchInfo) //
                .filter(Objects::nonNull) // null 안전성 추가
                .collect(Collectors.toList());

        log.info("총 {}건 매칭 완료 - 대상 날짜: {}", createdMatches.size(), targetDate);
        return new MatchingDto.Response(targetDate, createdMatches.size(), matchInfos, "배치 매칭이 성공적으로 완료되었습니다.");
    }

    // 잠재 매칭 후보 생성 (변경 없음)
    private List<PotentialMatch> generatePotentialMatches(List<MatchingDataDto.EmployerDetailInfo> details, List<MatchingDataDto.WorkerInfo> workers) { //
        List<PotentialMatch> potentials = new ArrayList<>();
        // WorkerInfo DTO에 neighborhoodId가 포함되어 있으므로 그룹핑 가능
        Map<Long, List<MatchingDataDto.WorkerInfo>> workersByLocation = workers.stream()
                .collect(Collectors.groupingBy(MatchingDataDto.WorkerInfo::neighborhoodId)); //

        for (MatchingDataDto.EmployerDetailInfo detail : details) { //
            // 업주 요청의 locationId (실제로는 neighborhoodId) 기준 조회
            List<MatchingDataDto.WorkerInfo> workersInLocation = workersByLocation.getOrDefault(detail.locationId(), Collections.emptyList()); //
            for (MatchingDataDto.WorkerInfo worker : workersInLocation) { //
                // 근로자가 원하는 jobTypeId 목록에 업주 요청의 jobTypeId가 포함되는지 확인
                if (worker.jobTypeIds().contains(detail.jobTypeId())) { //
                    potentials.add(new PotentialMatch(detail, worker));
                }
            }
        }
        log.debug("잠재 매칭 후보 생성 완료 ({} 건)", potentials.size());
        return potentials;
    }

    // 업주 요청 상세별 우선순위 높은 근로자 잠정 선택 (TentativeAssignment 수정됨)
    private List<TentativeAssignment> selectTopWorkersForEachDetail(List<PotentialMatch> potentialMatches) {
        Map<Long, List<PotentialMatch>> groupedByDetail = potentialMatches.stream()
                .collect(Collectors.groupingBy(p -> p.detail().detailId()));

        List<TentativeAssignment> tentativeAssignments = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<Long, List<PotentialMatch>> entry : groupedByDetail.entrySet()) {
            List<PotentialMatch> candidates = entry.getValue();
            if (candidates.isEmpty()) continue;

            int spotsAvailable = candidates.get(0).detail().spotsAvailable(); //

            List<PotentialMatch> selectedCandidates;

            // 정렬 로직 (변경 없음)
            if (candidates.size() > spotsAvailable) {
                candidates.sort((p1, p2) -> {
                    int scoreCompare = p2.worker().priorityScore().compareTo(p1.worker().priorityScore()); //
                    return scoreCompare != 0 ? scoreCompare : Integer.compare(random.nextInt(), random.nextInt());
                });
                selectedCandidates = candidates.stream().limit(spotsAvailable).collect(Collectors.toList());
                log.debug("Detail ID {}: {}명 지원, {}명 필요 -> 우선순위 적용하여 {}명 선택", entry.getKey(), candidates.size(), spotsAvailable, selectedCandidates.size());
            } else {
                selectedCandidates = candidates;
                log.debug("Detail ID {}: {}명 지원, {}명 필요 -> 전원 선택 ({}명)", entry.getKey(), candidates.size(), spotsAvailable, selectedCandidates.size());
            }

            // TentativeAssignment 생성 시 neighborhoodId 추가
            selectedCandidates.forEach(p -> tentativeAssignments.add(
                    new TentativeAssignment(
                            p.detail().detailId(), //
                            p.worker().workerId(), //
                            p.worker().workerRequestId(), //
                            p.worker().neighborhoodId(), // <<< neighborhoodId 추가 //
                            p.worker().priorityScore() //
                    )
            ));
        }
        log.debug("업주 요청별 근로자 잠정 선택 완료 (총 {} 건)", tentativeAssignments.size());
        return tentativeAssignments;
    }

    // 근로자별 중복 할당 해결 (FinalMatch 수정됨)
    private List<FinalMatch> resolveWorkerConflicts(List<TentativeAssignment> tentativeAssignments) {
        Map<Long, List<TentativeAssignment>> groupedByWorker = tentativeAssignments.stream()
                .collect(Collectors.groupingBy(TentativeAssignment::workerId)); // workerId 기준 그룹핑 유지

        List<FinalMatch> finalSelections = new ArrayList<>();
        Random random = new Random(); // 랜덤 선택용

        for (Map.Entry<Long, List<TentativeAssignment>> entry : groupedByWorker.entrySet()) {
            Long workerId = entry.getKey();
            List<TentativeAssignment> assignments = entry.getValue();

            TentativeAssignment finalAssignment;
            if (assignments.size() == 1) {
                finalAssignment = assignments.get(0);
            } else {
                // 여러 건일 경우 랜덤 선택 (로직 유지)
                Collections.shuffle(assignments, random);
                finalAssignment = assignments.get(0);
                log.debug("근로자 ID {} 중복 할당 해결: {}개 중 랜덤 선택 -> Detail ID {}", workerId, assignments.size(), finalAssignment.detailId());
            }
            // FinalMatch 생성 시 neighborhoodId 추가
            finalSelections.add(new FinalMatch(
                    finalAssignment.detailId(),
                    finalAssignment.workerRequestId(),
                    finalAssignment.neighborhoodId(), // <<< neighborhoodId 추가
                    finalAssignment.workerId()
            ));
        }
        log.debug("근로자 중복 할당 해결 완료 (최종 {} 건)", finalSelections.size());
        return finalSelections;
    }


    // 최종 선택된 정보로 Match 엔티티 생성 및 저장 (WorkerRequest 참조 및 Match 빌더 수정됨)
    private List<Match> createAndSaveMatches(List<FinalMatch> finalSelections, LocalDateTime matchTime) { //
        if (CollectionUtils.isEmpty(finalSelections)) {
            return List.of();
        }

        List<Match> matchesToSave = new ArrayList<>();
        for (FinalMatch fm : finalSelections) {
            try {
                EmployerRequestDetail detailRef = entityManager.getReference(EmployerRequestDetail.class, fm.detailId); //
                // WorkerRequest 참조 시 복합키 사용
                WorkerRequestId workerReqCompositeId = new WorkerRequestId(fm.workerRequestId, fm.neighborhoodId); //
                WorkerRequest workerRequestRef = entityManager.getReference(WorkerRequest.class, workerReqCompositeId); //
                Worker workerRef = entityManager.getReference(Worker.class, fm.workerId); //

                Match matchEntity = Match.builder() //
                        .employerRequestDetail(detailRef)
                        .workerRequest(workerRequestRef) // 참조 방식 변경됨
                        .worker(workerRef)
                        .matchDatetime(matchTime)
                        .build();
                matchesToSave.add(matchEntity);
            } catch (EntityNotFoundException e) { // jakarta.persistence.EntityNotFoundException 사용
                log.error("Match 엔티티 생성 중 참조 에러 발생 (WorkerRequest PK: {},{} / Worker PK: {}): {}",
                        fm.workerRequestId, fm.neighborhoodId, fm.workerId, e.getMessage());
            } catch (Exception e) {
                log.error("Match 엔티티 생성 중 예상치 못한 오류 발생: {}", fm, e);
            }
        }

        if (matchesToSave.isEmpty()) {
            log.warn("저장할 유효한 Match 엔티티가 없습니다. (finalSelections size: {})", finalSelections.size());
            return List.of();
        }

        return matchRepository.saveAll(matchesToSave); //
    }


    // 상태 업데이트 로직 (WorkerRequest 업데이트 조건 수정됨)
    private void updateStatusesAfterMatching(List<Match> createdMatches) { //
        if (createdMatches == null || createdMatches.isEmpty()) return;
        log.info("매칭 후 상태 업데이트 시작...");

        // EmployerRequestDetail ID 수집 (변경 없음)
        Set<Long> matchedErdIds = createdMatches.stream()
                .map(m -> m.getEmployerRequestDetail().getRequestDetailId()) //
                .collect(Collectors.toSet());

        // WorkerRequest 복합키 수집 (변경됨)
        Set<WorkerRequestId> matchedWrCompositeIds = createdMatches.stream()
                .map(m -> new WorkerRequestId(m.getWorkerRequest().getWorkerRequestId(), m.getWorkerRequest().getNeighborhoodId())) //
                .collect(Collectors.toSet());

        // EmployerRequest ID 수집 (변경 없음)
        Set<Long> matchedErIds = createdMatches.stream()
                .map(m -> m.getEmployerRequestDetail().getEmployerRequest().getRequestId()) //
                .collect(Collectors.toSet());

        // 1. EmployerRequestDetail matched_count 업데이트 (변경 없음)
        if (!matchedErdIds.isEmpty()) {
            Map<Long, Long> erdMatchCounts = createdMatches.stream()
                    .collect(Collectors.groupingBy(m -> m.getEmployerRequestDetail().getRequestDetailId(), Collectors.counting())); //
            List<EmployerRequestDetail> detailsToUpdate = employerRequestDetailRepository.findAllById(matchedErdIds); //
            detailsToUpdate.forEach(detail -> { //
                long newlyMatched = erdMatchCounts.getOrDefault(detail.getRequestDetailId(), 0L);
                if (newlyMatched > 0) {
                    detail.setMatchedCount(detail.getMatchedCount() + (int) newlyMatched);
                }
            });
            log.info("{} 건의 EmployerRequestDetail matched_count 업데이트 대상 처리", detailsToUpdate.size());
        }

        // 2. WorkerRequest 상태 업데이트 (BooleanBuilder 사용 방식으로 수정)
        if (!matchedWrCompositeIds.isEmpty()) {
            // BooleanBuilder를 사용하여 복합키 조건 동적 생성
            com.querydsl.core.BooleanBuilder predicate = new com.querydsl.core.BooleanBuilder(); // Full path 명시 또는 import 추가
            for (WorkerRequestId wrId : matchedWrCompositeIds) {
                predicate.or(
                        workerRequest.workerRequestId.eq(wrId.getWorkerRequestId())
                                .and(workerRequest.neighborhoodId.eq(wrId.getNeighborhoodId()))
                );
            }

            long updatedWrCount = queryFactory.update(workerRequest)
                    .set(workerRequest.status, RequestStatus.FULLY_MATCHED)
                    .where(
                            predicate, // 생성된 OR 조건 적용
                            workerRequest.status.ne(RequestStatus.CLOSED),
                            workerRequest.status.ne(RequestStatus.CANCELLED)
                    )
                    .execute();
            log.info("{} 건의 WorkerRequest 상태 업데이트됨", updatedWrCount);
        }

        // 3. EmployerRequest 상태 업데이트 (변경 없음)
        if (!matchedErIds.isEmpty()) {
            List<EmployerRequest> requestsToUpdate = queryFactory.selectFrom(employerRequest).distinct() //
                    .leftJoin(employerRequest.details, employerRequestDetail).fetchJoin() //
                    .where(employerRequest.requestId.in(matchedErIds),
                            employerRequest.status.ne(RequestStatus.CLOSED),
                            employerRequest.status.ne(RequestStatus.CANCELLED),
                            employerRequest.status.ne(RequestStatus.FULLY_MATCHED))
                    .fetch();

            requestsToUpdate.forEach(req -> { //
                // getDetails() 호출 시 fetch join으로 가져온 프록시가 아닌 실제 엔티티 사용됨
                int totalRequired = req.getDetails().stream().mapToInt(EmployerRequestDetail::getRequiredCount).sum(); //
                int totalMatched = req.getDetails().stream().mapToInt(EmployerRequestDetail::getMatchedCount).sum(); //
                if (totalMatched >= totalRequired) {
                    req.setStatus(RequestStatus.FULLY_MATCHED);
                } else if (totalMatched > 0) {
                    req.setStatus(RequestStatus.PARTIALLY_MATCHED);
                }
                // PENDING 상태는 유지될 수 있음 (totalMatched == 0)
            });
            log.info("{} 건의 EmployerRequest 상태 업데이트 대상 처리", requestsToUpdate.size());
        }
        log.info("매칭 후 상태 업데이트 완료.");
    }


    // Match 엔티티 -> DTO 변환 (Neighborhood 조회 방식 변경)
    private MatchingDto.MatchInfo mapMatchToMatchInfo(Match match) { //
        if (match == null) { // null 체크 추가
            return null;
        }
        // Optional 및 orElseGet 사용하여 Null 안전성 확보
        Worker matchedWorker = Optional.ofNullable(match.getWorker()).orElseGet(Worker::new); //
        EmployerRequestDetail matchedDetail = Optional.ofNullable(match.getEmployerRequestDetail()).orElseGet(EmployerRequestDetail::new); //
        EmployerRequest matchedRequest = Optional.ofNullable(matchedDetail.getEmployerRequest()).orElseGet(EmployerRequest::new); //
        Employer matchedEmployer = Optional.ofNullable(matchedRequest.getEmployer()).orElseGet(Employer::new); //
        JobType matchedJobType = Optional.ofNullable(matchedDetail.getJobType()).orElseGet(JobType::new); //

        // Neighborhood 정보 조회: Match 엔티티의 neighborhoodId 사용
        Neighborhood matchedNeighborhood = Optional.ofNullable(match.getWorkerRequest().getNeighborhoodId()) //
                .flatMap(neighborhoodRepository::findById) // ID로 Neighborhood 조회 //
                .orElseGet(() -> {
                    // Fallback: EmployerRequest의 locationNeighborhood 사용 (하지만 Match의 ID를 우선)
                    log.warn("Match ID {}에 해당하는 Neighborhood ID {}를 찾을 수 없어 EmployerRequest의 위치 정보로 대체합니다.", match.getMatchId(), match.getWorkerRequest().getNeighborhoodId()); //
                    return Optional.ofNullable(matchedRequest.getLocationNeighborhood()).orElseGet(Neighborhood::new); //
                });


        return MatchingDto.MatchInfo.builder() //
                .matchId(match.getMatchId()) //
                .workerId(matchedWorker.getId()) //
                .workerName(matchedWorker.getName()) //
                .workerPhoneNumber(matchedWorker.getPhoneNumber()) //
                .employerId(matchedEmployer.getId()) //
                .businessName(matchedEmployer.getBusinessName()) //
                .jobTypeId(matchedJobType.getJobTypeId()) //
                .jobTypeName(matchedJobType.getName()) //
                .neighborhoodId(matchedNeighborhood.getNeighborhoodId()) // 조회된 Neighborhood 정보 사용 //
                .neighborhoodName(matchedNeighborhood.getNeighborhoodName()) // 조회된 Neighborhood 정보 사용 //
                .matchDateTime(match.getMatchDatetime()) //
                .build();
    }

    // 빈 결과 반환 헬퍼 (변경 없음)
    private MatchingDto.Response createEmptyResponse(LocalDate targetDate, String message) { //
        return new MatchingDto.Response(targetDate, 0, List.of(), "QueryDSL (Repo) + Java Logic" + message);
    }

    // --- 내부 헬퍼 레코드 (TentativeAssignment, FinalMatch 수정됨) ---
    private record PotentialMatch(MatchingDataDto.EmployerDetailInfo detail, MatchingDataDto.WorkerInfo worker) {} //
    // TentativeAssignment: neighborhoodId 추가
    private record TentativeAssignment(Long detailId, Long workerId, Long workerRequestId, Long neighborhoodId, Double priorityScore) {}
    // FinalMatch: neighborhoodId 추가
    private record FinalMatch(Long detailId, Long workerRequestId, Long neighborhoodId, Long workerId) {}
}