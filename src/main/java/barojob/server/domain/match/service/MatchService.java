package barojob.server.domain.match.service;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.employer.entity.EmployerRequestDetail;
import barojob.server.domain.employer.repository.EmployerRequestDetailRepository;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.match.dto.MatchingDto;
import barojob.server.domain.match.entity.Match;
import barojob.server.domain.match.repository.MatchRepository;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
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
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

//    @Transactional
//    public MatchingDto.Response performDailyBatchMatching(LocalDateTime executionTime) {
//
//        // 1. 대상 날짜 설정
//        LocalDate targetDate = executionTime.toLocalDate().plusDays(1);
//        log.info("배치 매칭 프로세스 시작 - 대상 날짜: {}", targetDate);
//
//        /*
//        todo: 2. 성능 + 방어적 프로그래밍 관점으로 매칭 대상 업주, 근로자 뽑을 때 이미 매칭된 애들은 아에 제외해서 3,4 파라미터로 넘기고 시작
//                최적화 할때 해봐야 할듯
//        */
//
//        // 3. 매칭 가능한 업주 요청 상세 정보 조회
//        List<MatchingDataDto.EmployerDetailInfo> eligibleDetails = employerRequestDetailRepository.findEligibleDetailInfoForMatching(targetDate);
//        log.debug("매칭 가능 업주 요청 상세 정보 수: {}", eligibleDetails.size());
//        if (eligibleDetails.isEmpty()) {
//            return createEmptyResponse(targetDate, "매칭 대상 업주 요청이 없습니다.");
//        }
//
//        // 4. 매칭 가능한 근로자 정보 조회
//        List<MatchingDataDto.WorkerInfo> eligibleWorkers = workerRequestRepository.findEligibleWorkerInfoForMatching(targetDate);
//        log.debug("매칭 가능 근로자 정보 수: {}", eligibleWorkers.size());
//        if (eligibleWorkers.isEmpty()) {
//            return createEmptyResponse(targetDate, "매칭 대상 근로자 요청이 없습니다.");
//        }
//
//        // 5. 잠재적 매칭 후보 생성 (지역 & 직종 일치하면 PotentailMatch로 일단 싹 다 뽑음)
//        List<PotentialMatch> potentialMatches = generatePotentialMatches(eligibleDetails, eligibleWorkers);
//
//        // 6. 업주 요청 상세별 필요 인원수(spots)만큼 근로자 잠정 선택
//        List<TentativeAssignment> tentativeAssignments = selectTopWorkersForEachDetail(potentialMatches);
//
//        // 7. 근로자별 중복 할당 해결
//        List<FinalMatch> finalSelections = resolveWorkerConflicts(tentativeAssignments);
//
//        // --- 최종 매칭 결과 저장 및 상태 업데이트 ---
//
//        // 8. 최종 선택된 정보로 Match 엔티티 생성 및 저장
//        List<Match> createdMatches = createAndSaveMatches(finalSelections, executionTime);
//
//        if (CollectionUtils.isEmpty(createdMatches)) {
//            log.info("새로 생성된 매칭이 없습니다.");
//            return createEmptyResponse(targetDate, "매칭된 결과가 없습니다.");
//        }
//
//        // 9. 상태 업데이트 (matchedCount 증가, status 변경)
//        updateStatusesAfterMatching(createdMatches);
//
//        // 10. 결과 DTO 생성 및 반환
//        List<MatchingDto.MatchInfo> matchInfos = createdMatches.stream()
//                .map(this::mapMatchToMatchInfo)
//                .collect(Collectors.toList()); // 여기서 Null 필터링은 제거됨
//
//        log.info("총 {}건 매칭 완료 - 대상 날짜: {}", createdMatches.size(), targetDate);
//        return new MatchingDto.Response(targetDate, createdMatches.size(), matchInfos, "배치 매칭이 성공적으로 완료되었습니다.");
//    }


    // 잠재 매칭 후보 생성
    private List<PotentialMatch> generatePotentialMatches(List<MatchingDataDto.EmployerDetailInfo> details, List<MatchingDataDto.WorkerInfo> workers) {
        List<PotentialMatch> potentials = new ArrayList<>();

        Map<Long, List<MatchingDataDto.WorkerInfo>> workersByLocation = workers.stream()
                .flatMap(w -> w.neighborhoodIds().stream().map(nid -> Map.entry(nid, w)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (MatchingDataDto.EmployerDetailInfo detail : details) {
            // 해당 지역(detail.locationId())을 선호하는 근로자 리스트 가져오기
            List<MatchingDataDto.WorkerInfo> potentialWorkers = workersByLocation.getOrDefault(detail.locationId(), Collections.emptyList());

            for (MatchingDataDto.WorkerInfo worker : potentialWorkers) {
                // 해당 직종(detail.jobTypeId())을 수행 가능한지 확인
                if (worker.jobTypeIds().contains(detail.jobTypeId())) {
                    potentials.add(new PotentialMatch(detail, worker));
                }
            }
        }
        log.debug("잠재 매칭 후보 생성 완료 ({} 건)", potentials.size());
        return potentials;
    }


    // 업주 요청 상세별 우선순위 높은 근로자 잠정 선택
    private List<TentativeAssignment> selectTopWorkersForEachDetail(List<PotentialMatch> potentialMatches) {
        // 그룹화 시 Null 체크 제거됨
        Map<Long, List<PotentialMatch>> groupedByDetail = potentialMatches.stream()
                .collect(Collectors.groupingBy(p -> p.detail().detailId()));

        List<TentativeAssignment> tentativeAssignments = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<Long, List<PotentialMatch>> entry : groupedByDetail.entrySet()) {
            List<PotentialMatch> candidates = entry.getValue();
            if (candidates.isEmpty()) continue;

            int spotsAvailable = candidates.get(0).detail().spotsAvailable();

            List<PotentialMatch> selectedCandidates;

            // 필요 인원보다 지원자가 많을 경우에만 우선순위+랜덤 정렬 후 상위 선택
            if (candidates.size() > spotsAvailable) {
                candidates.sort((p1, p2) -> {
                    int scoreCompare = p2.worker().priorityScore().compareTo(p1.worker().priorityScore());
                    return scoreCompare != 0 ? scoreCompare : Integer.compare(random.nextInt(), random.nextInt());
                });
                selectedCandidates = candidates.stream().limit(spotsAvailable).collect(Collectors.toList());
                log.debug("Detail ID {}: {}명 지원, {}명 필요 -> 우선순위 적용하여 {}명 선택", entry.getKey(), candidates.size(), spotsAvailable, selectedCandidates.size());
            } else {
                // 필요 인원보다 지원자가 적거나 같으면 모두 선택
                selectedCandidates = candidates;
                log.debug("Detail ID {}: {}명 지원, {}명 필요 -> 전원 선택 ({}명)", entry.getKey(), candidates.size(), spotsAvailable, selectedCandidates.size());
            }

            selectedCandidates.forEach(p -> tentativeAssignments.add(
                    new TentativeAssignment(p.detail().detailId(), p.worker().workerId(), p.worker().workerRequestId(), p.worker().priorityScore())
            ));
        }
        log.debug("업주 요청별 근로자 잠정 선택 완료 (총 {} 건)", tentativeAssignments.size());
        return tentativeAssignments;
    }


    // 근로자별 중복 할당 해결
    private List<FinalMatch> resolveWorkerConflicts(List<TentativeAssignment> tentativeAssignments) {
        Map<Long, List<TentativeAssignment>> groupedByWorker = tentativeAssignments.stream()
                .collect(Collectors.groupingBy(TentativeAssignment::workerId));

        List<FinalMatch> finalSelections = new ArrayList<>();
        Random random = new Random(); // 랜덤 선택용

        for (Map.Entry<Long, List<TentativeAssignment>> entry : groupedByWorker.entrySet()) {
            Long workerId = entry.getKey();
            List<TentativeAssignment> assignments = entry.getValue();

            TentativeAssignment finalAssignment;
            if (assignments.size() == 1) {
                finalAssignment = assignments.get(0);
            } else {
                // 여러 건일 경우 랜덤 선택
                Collections.shuffle(assignments, random);
                finalAssignment = assignments.get(0);
                log.debug("근로자 ID {} 중복 할당 해결: {}개 중 랜덤 선택 -> Detail ID {}", workerId, assignments.size(), finalAssignment.detailId());
            }
            finalSelections.add(new FinalMatch(finalAssignment.detailId(), finalAssignment.workerRequestId(), finalAssignment.workerId()));
        }
        log.debug("근로자 중복 할당 해결 완료 (최종 {} 건)", finalSelections.size());
        return finalSelections;
    }


    // 최종 선택된 정보로 Match 엔티티 생성 및 저장
    private List<Match> createAndSaveMatches(List<FinalMatch> finalSelections, LocalDateTime matchTime) {
        if (CollectionUtils.isEmpty(finalSelections)) {
            return List.of();
        }

        List<Match> matchesToSave = new ArrayList<>();
        for (FinalMatch fm : finalSelections) {
            try {
                EmployerRequestDetail detailRef = entityManager.getReference(EmployerRequestDetail.class, fm.detailId);
                WorkerRequest workerRequestRef = entityManager.getReference(WorkerRequest.class, fm.workerRequestId);
                Worker workerRef = entityManager.getReference(Worker.class, fm.workerId);

                Match matchEntity = Match.builder()
                        .employerRequestDetail(detailRef)
                        .workerRequest(workerRequestRef)
                        .worker(workerRef)
                        .matchDatetime(matchTime)
                        .build();
                matchesToSave.add(matchEntity);
            } catch (jakarta.persistence.EntityNotFoundException e) {
                log.error("Match 엔티티 생성 중 참조 에러 발생: {}", fm, e);
            } catch (Exception e) { // 기본적인 예외 처리
                log.error("Match 엔티티 생성 중 예상치 못한 오류 발생: {}", fm, e);
            }
        }

        return matchRepository.saveAll(matchesToSave);
    }


    // 상태 업데이트 로직 (JPA 변경 감지 및 QueryDSL 벌크 업데이트)
    private void updateStatusesAfterMatching(List<Match> createdMatches) {
        if (createdMatches == null || createdMatches.isEmpty()) return;
        log.info("매칭 후 상태 업데이트 시작...");

        Set<Long> matchedErdIds = createdMatches.stream().map(m -> m.getEmployerRequestDetail().getRequestDetailId()).collect(Collectors.toSet());
        Set<Long> matchedWrIds = createdMatches.stream().map(m -> m.getWorkerRequest().getWorkerRequestId()).collect(Collectors.toSet());
        Set<Long> matchedErIds = createdMatches.stream().map(m -> m.getEmployerRequestDetail().getEmployerRequest().getRequestId()).collect(Collectors.toSet());

        // 1. EmployerRequestDetail matched_count 업데이트
        if (!matchedErdIds.isEmpty()) {
            // try-catch 제거됨
            Map<Long, Long> erdMatchCounts = createdMatches.stream()
                    .collect(Collectors.groupingBy(m -> m.getEmployerRequestDetail().getRequestDetailId(), Collectors.counting()));
            List<EmployerRequestDetail> detailsToUpdate = employerRequestDetailRepository.findAllById(matchedErdIds);
            detailsToUpdate.forEach(detail -> {
                long newlyMatched = erdMatchCounts.getOrDefault(detail.getRequestDetailId(), 0L);
                if (newlyMatched > 0) {
                    detail.setMatchedCount(detail.getMatchedCount() + (int) newlyMatched);
                }
            });
            log.info("{} 건의 EmployerRequestDetail matched_count 업데이트 대상 처리", detailsToUpdate.size());
        }

        // 2. WorkerRequest 상태 업데이트 (QueryDSL 벌크)
        if (!matchedWrIds.isEmpty()) {
            long updatedWrCount = queryFactory.update(workerRequest)
                    .set(workerRequest.status, RequestStatus.FULLY_MATCHED)
                    .where(workerRequest.workerRequestId.in(matchedWrIds),
                            workerRequest.status.ne(RequestStatus.CLOSED),
                            workerRequest.status.ne(RequestStatus.CANCELLED))
                    .execute();
            log.info("{} 건의 WorkerRequest 상태 업데이트됨", updatedWrCount);
        }

        // 3. EmployerRequest 상태 업데이트 (변경 감지 + Fetch Join)
        if (!matchedErIds.isEmpty()) {
            List<EmployerRequest> requestsToUpdate = queryFactory.selectFrom(employerRequest).distinct()
                    .leftJoin(employerRequest.details, employerRequestDetail).fetchJoin()
                    .where(employerRequest.requestId.in(matchedErIds),
                            employerRequest.status.ne(RequestStatus.CLOSED),
                            employerRequest.status.ne(RequestStatus.CANCELLED),
                            employerRequest.status.ne(RequestStatus.FULLY_MATCHED))
                    .fetch();

            requestsToUpdate.forEach(req -> {
                int totalRequired = req.getDetails().stream().mapToInt(EmployerRequestDetail::getRequiredCount).sum();
                int totalMatched = req.getDetails().stream().mapToInt(EmployerRequestDetail::getMatchedCount).sum();
                if (totalMatched >= totalRequired) {
                    req.setStatus(RequestStatus.FULLY_MATCHED);
                } else if (totalMatched > 0) {
                    req.setStatus(RequestStatus.PARTIALLY_MATCHED);
                }
            });
            log.info("{} 건의 EmployerRequest 상태 업데이트 대상 처리", requestsToUpdate.size());
        }
        log.info("매칭 후 상태 업데이트 완료.");
    }


    // Match 엔티티 -> DTO 변환
    private MatchingDto.MatchInfo mapMatchToMatchInfo(Match match) {
        Worker matchedWorker = Optional.ofNullable(match.getWorker()).orElseGet(Worker::new);
        EmployerRequestDetail matchedDetail = Optional.ofNullable(match.getEmployerRequestDetail()).orElseGet(EmployerRequestDetail::new);
        EmployerRequest matchedRequest = Optional.ofNullable(matchedDetail.getEmployerRequest()).orElseGet(EmployerRequest::new);
        Employer matchedEmployer = Optional.ofNullable(matchedRequest.getEmployer()).orElseGet(Employer::new);
        Neighborhood matchedNeighborhood = Optional.ofNullable(matchedRequest.getLocationNeighborhood()).orElseGet(Neighborhood::new);
        JobType matchedJobType = Optional.ofNullable(matchedDetail.getJobType()).orElseGet(JobType::new);

        return MatchingDto.MatchInfo.builder()
                .matchId(match.getMatchId())
                .workerId(matchedWorker.getId())
                .workerName(matchedWorker.getName())
                .workerPhoneNumber(matchedWorker.getPhoneNumber())
                .employerId(matchedEmployer.getId())
                .businessName(matchedEmployer.getBusinessName())
                .jobTypeId(matchedJobType.getJobTypeId())
                .jobTypeName(matchedJobType.getName())
                .neighborhoodId(matchedNeighborhood.getNeighborhoodId())
                .neighborhoodName(matchedNeighborhood.getNeighborhoodName())
                .matchDateTime(match.getMatchDatetime())
                .build();
    }


    // 빈 결과 반환 헬퍼
    private MatchingDto.Response createEmptyResponse(LocalDate targetDate, String message) {
        return new MatchingDto.Response(targetDate, 0, List.of(), "QueryDSL (Repo) + Java Logic" + message);
    }


    // --- 내부 헬퍼 클래스/레코드 ---
    private record PotentialMatch(MatchingDataDto.EmployerDetailInfo detail, MatchingDataDto.WorkerInfo worker) {}
    private record TentativeAssignment(Long detailId, Long workerId, Long workerRequestId, Double priorityScore) {}
    private record FinalMatch(Long detailId, Long workerRequestId, Long workerId) {}
}