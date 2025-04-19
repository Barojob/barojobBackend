package barojob.server.domain.worker.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.worker.dto.WorkerRequestDto.ManualMatchingResponse;
import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.QWorkerRequest;
import barojob.server.domain.worker.entity.QWorkerRequestJobType;
import barojob.server.domain.worker.entity.QWorkerRequestLocation;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static barojob.server.domain.jobType.entity.QJobType.jobType;
import static barojob.server.domain.location.entity.QNeighborhood.neighborhood;
import static barojob.server.domain.worker.entity.QWorker.worker;
import static barojob.server.domain.worker.entity.QWorkerRequest.workerRequest;
import static barojob.server.domain.worker.entity.QWorkerRequestJobType.workerRequestJobType;
import static barojob.server.domain.worker.entity.QWorkerRequestLocation.workerRequestLocation;

@RequiredArgsConstructor
public class WorkerRequestRepositoryCustomImpl implements WorkerRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ManualMatchingResponse> findWorkerRequestPageByNeighborhoodAndJobType(
            Long neighborhoodId,
            Long jobTypeId,
            Pageable pageable) {

        QWorkerRequest wr = QWorkerRequest.workerRequest;
        QWorker w = QWorker.worker;
        QWorkerRequestLocation loc = QWorkerRequestLocation.workerRequestLocation;
        QWorkerRequestJobType jt = QWorkerRequestJobType.workerRequestJobType;

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));


        List<ManualMatchingResponse> contents = queryFactory
                .select(Projections.constructor(
                        ManualMatchingResponse.class,
                        wr.workerRequestId,
                        w.name.as("workerName"),
                        w.phoneNumber,
                        w.priorityScore
                ))
                .from(wr)
                .join(wr.worker, w)
                .join(wr.locations, loc)
                .join(wr.jobTypes, jt)
                .where(
                        wr.requestDate.eq(today),
                        wr.status.eq(RequestStatus.PENDING),
                        loc.neighborhood.neighborhoodId.eq(neighborhoodId),
                        jt.jobType.jobTypeId.eq(jobTypeId)
                )
                .distinct()  // WorkerRequest 중복 제거
                .orderBy(w.priorityScore.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory
                .select(wr.countDistinct())
                .from(wr)
                .join(wr.locations, loc)
                .join(wr.jobTypes, jt)
                .where(
                        wr.requestDate.eq(today),
                        wr.status.eq(RequestStatus.PENDING),
                        loc.neighborhood.neighborhoodId.eq(neighborhoodId),
                        jt.jobType.jobTypeId.eq(jobTypeId)
                )
                .fetchOne();

        return new PageImpl<>(contents, pageable, total != null ? total : 0L);
    }

    @Override
    public List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate) {
        // 1단계: 기본 정보 조회 -> 지역, 업종 정보 아직 x
        // Tuple을 사용하여 여러 필드를 한 번에 조회
        List<Tuple> basicInfoTuples = queryFactory
                .select(workerRequest.workerRequestId, worker.id, worker.priorityScore)
                .from(workerRequest)
                .join(workerRequest.worker, worker)
                .where(
                        workerRequest.requestDate.eq(targetDate),
                        workerRequest.status.in(RequestStatus.PENDING)
                )
                .fetch();

        if (basicInfoTuples.isEmpty()) {
            return Collections.emptyList();
        }

        //id만 싹 뽑음
        List<Long> workerRequestIds = basicInfoTuples.stream()
                .map(t -> t.get(workerRequest.workerRequestId)) // Tuple에서 workerRequestId 추출
                .distinct() // 중복 제거 (혹시 모를 경우 대비)
                .collect(Collectors.toList());

        // 2단계: 지역 및 직종 ID 일괄 조회 (수정된 메소드 호출)
        Map<Long, Set<Long>> locationIdsMap = findNeighborhoodIdsByRequestIdsGrouped(workerRequestIds);
        Map<Long, Set<Long>> jobTypeIdsMap = findJobTypeIdsByRequestIdsGrouped(workerRequestIds);

        // 3단계: 정보 조합
        return basicInfoTuples.stream().map(tuple -> {
            Long wrId = tuple.get(workerRequest.workerRequestId);
            Long wId = tuple.get(worker.id);
            Double score = tuple.get(worker.priorityScore);
            // 기본값으로 빈 Set을 제공하여 NullPointerException 방지
            Set<Long> locationIds = locationIdsMap.getOrDefault(wrId, Collections.emptySet());
            Set<Long> jobTypeIds = jobTypeIdsMap.getOrDefault(wrId, Collections.emptySet());
            return new MatchingDataDto.WorkerInfo(wrId, wId, score, locationIds, jobTypeIds);
        }).collect(Collectors.toList());
    }

    //결과: Map<요청ID, 지역ID Set>
    private Map<Long, Set<Long>> findNeighborhoodIdsByRequestIdsGrouped(List<Long> workerRequestIds) {
        if (CollectionUtils.isEmpty(workerRequestIds)) return Collections.emptyMap();

        List<Tuple> results = queryFactory
                .select(workerRequestLocation.workerRequest.workerRequestId,
                        workerRequestLocation.neighborhood.neighborhoodId)
                .from(workerRequestLocation)
                .join(workerRequestLocation.neighborhood, neighborhood)
                .where(workerRequestLocation.workerRequest.workerRequestId.in(workerRequestIds))
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(workerRequestLocation.workerRequest.workerRequestId),
                        Collectors.mapping(tuple -> tuple.get(workerRequestLocation.neighborhood.neighborhoodId), Collectors.toSet())
                ));
    }

    //결과: Map<요청ID, 직종ID Set>
    private Map<Long, Set<Long>> findJobTypeIdsByRequestIdsGrouped(List<Long> workerRequestIds) {
        if (CollectionUtils.isEmpty(workerRequestIds)) return Collections.emptyMap();

        List<Tuple> results = queryFactory
                .select(workerRequestJobType.workerRequest.workerRequestId,
                        workerRequestJobType.jobType.jobTypeId)
                .from(workerRequestJobType)
                .join(workerRequestJobType.jobType, jobType)
                .where(workerRequestJobType.workerRequest.workerRequestId.in(workerRequestIds))
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(workerRequestJobType.workerRequest.workerRequestId),
                        Collectors.mapping(tuple -> tuple.get(workerRequestJobType.jobType.jobTypeId), Collectors.toSet())
                ));
    }
}