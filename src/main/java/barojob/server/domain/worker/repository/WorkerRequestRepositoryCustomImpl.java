package barojob.server.domain.worker.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.worker.dto.WorkerRequestDto.ManualMatchingResponse;
import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.QWorkerRequest;
import barojob.server.domain.worker.entity.QWorkerRequestJobType;
import barojob.server.domain.worker.entity.WorkerRequestId;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static barojob.server.domain.jobType.entity.QJobType.jobType;
import static barojob.server.domain.location.entity.QNeighborhood.neighborhood;
import static barojob.server.domain.worker.entity.QWorker.worker;
import static barojob.server.domain.worker.entity.QWorkerRequest.workerRequest;
import static barojob.server.domain.worker.entity.QWorkerRequestJobType.workerRequestJobType;
import static com.querydsl.jpa.JPAExpressions.selectOne;

@RequiredArgsConstructor
public class WorkerRequestRepositoryCustomImpl implements WorkerRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ManualMatchingResponse> findWorkerRequestPageByNeighborhoodAndJobType(
            Long neighborhoodId,
            Long jobTypeId,             // 단일 jobTypeId
            Pageable pageable) {

        QWorkerRequest wr = QWorkerRequest.workerRequest;
        QWorker w  = QWorker.worker;
        QWorkerRequestJobType jr = QWorkerRequestJobType.workerRequestJobType;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<ManualMatchingResponse> contents = queryFactory
                .select(Projections.constructor(
                        ManualMatchingResponse.class,
                        wr.workerRequestId,
                        w.name,
                        w.phoneNumber,
                        wr.priorityScore
                ))
                .distinct()
                .from(wr)
                .join(wr.jobTypes, jr)
                .join(wr.worker, w)
                .where(
                        wr.neighborhoodId.eq(neighborhoodId),
                        wr.status.eq(RequestStatus.PENDING),
                        wr.requestDate.eq(today),
                        jr.jobType.jobTypeId.eq(jobTypeId)
                )
                .orderBy(wr.priorityScore.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(wr.workerRequestId.countDistinct())
                .from(wr)
                .join(wr.jobTypes, jr)
                .where(
                        wr.neighborhoodId.eq(neighborhoodId),
                        wr.status.eq(RequestStatus.PENDING),
                        wr.requestDate.eq(today),
                        jr.jobType.jobTypeId.eq(jobTypeId)
                )
                .fetchOne();

        return new PageImpl<>(
                contents,
                pageable,
                total == null ? 0L : total
        );
    }

    @Override
    public List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate) {
        List<Tuple> basicInfoTuples = queryFactory
                .select(
                        workerRequest.workerRequestId,
                        workerRequest.neighborhoodId,
                        workerRequest.worker.id,
                        workerRequest.priorityScore
                )
                .from(workerRequest)
                .join(workerRequest.worker, worker)
                .where(
                        workerRequest.requestDate.eq(targetDate),
                        workerRequest.status.eq(RequestStatus.PENDING)
                )
                .fetch();

        if (basicInfoTuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<WorkerRequestId> compositeKeys = basicInfoTuples.stream()
                .map(t -> new WorkerRequestId(t.get(workerRequest.workerRequestId), t.get(workerRequest.neighborhoodId)))
                .distinct()
                .collect(Collectors.toList());

        Map<WorkerRequestId, Set<Long>> jobTypeIdsMap = findJobTypeIdsByCompositeKeysGrouped(compositeKeys);

        return basicInfoTuples.stream().map(tuple -> {
            Long wrId = tuple.get(workerRequest.workerRequestId);
            Long nId = tuple.get(workerRequest.neighborhoodId);
            Long wId = tuple.get(workerRequest.worker.id);
            Double score = tuple.get(workerRequest.priorityScore);
            WorkerRequestId currentKey = new WorkerRequestId(wrId, nId);
            Set<Long> jobTypeIds = jobTypeIdsMap.getOrDefault(currentKey, Collections.emptySet());

            return new MatchingDataDto.WorkerInfo(wrId, wId, score, nId, jobTypeIds);
        }).collect(Collectors.toList());
    }

    private Map<WorkerRequestId, Set<Long>> findJobTypeIdsByCompositeKeysGrouped(List<WorkerRequestId> compositeKeys) {
        if (CollectionUtils.isEmpty(compositeKeys)) return Collections.emptyMap();

        List<Tuple> results = queryFactory
                .select(
                        workerRequestJobType.workerRequest.workerRequestId,
                        workerRequestJobType.workerRequest.neighborhoodId,
                        workerRequestJobType.jobType.jobTypeId
                )
                .from(workerRequestJobType)
                .where(
                        workerRequestJobType.workerRequest.workerRequestId.in(
                                compositeKeys.stream().map(WorkerRequestId::getWorkerRequestId).collect(Collectors.toSet())
                        )
                )
                .fetch();

        Set<WorkerRequestId> validKeys = new HashSet<>(compositeKeys);
        return results.stream()
                .filter(tuple -> {
                    WorkerRequestId key = new WorkerRequestId(
                            tuple.get(workerRequestJobType.workerRequest.workerRequestId),
                            tuple.get(workerRequestJobType.workerRequest.neighborhoodId)
                    );
                    return validKeys.contains(key);
                })
                .collect(Collectors.groupingBy(
                        tuple -> new WorkerRequestId(
                                tuple.get(workerRequestJobType.workerRequest.workerRequestId),
                                tuple.get(workerRequestJobType.workerRequest.neighborhoodId)
                        ),
                        Collectors.mapping(tuple -> tuple.get(workerRequestJobType.jobType.jobTypeId), Collectors.toSet())
                ));
    }

}