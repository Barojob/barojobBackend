package barojob.server.domain.worker.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.jobType.entity.QJobType;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.dto.WorkerRequestDto.ManualMatchingResponse;
import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.QWorkerRequest;
import barojob.server.domain.worker.entity.QWorkerRequestJobType;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static barojob.server.domain.jobType.entity.QJobType.jobType;
import static barojob.server.domain.worker.entity.QWorker.worker;
import static barojob.server.domain.worker.entity.QWorkerRequest.workerRequest;
import static barojob.server.domain.worker.entity.QWorkerRequestJobType.workerRequestJobType;

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
        QWorkerRequestJobType jr = QWorkerRequestJobType.workerRequestJobType;

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<ManualMatchingResponse> contents = queryFactory
                .select(Projections.constructor(
                        ManualMatchingResponse.class,
                        wr.workerRequestId,
                        w.name,
                        w.phoneNumber,
                        wr.priorityScore))
                .from(wr)
                .join(wr.worker, w)
                .where(
                        wr.neighborhoodId.eq(neighborhoodId),
                        wr.status.eq(RequestStatus.PENDING),
                        wr.requestDate.eq(today),
                        JPAExpressions
                                .selectOne()
                                .from(jr)
                                .where(
                                        jr.workerRequest.eq(wr),
                                        jr.jobType.jobTypeId.eq(jobTypeId)
                                )
                                .exists()
                )
                .orderBy(wr.priorityScore.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(wr.count())
                .from(wr)
                .where(
                        wr.neighborhoodId.eq(neighborhoodId),
                        wr.status.eq(RequestStatus.PENDING),
                        wr.requestDate.eq(today),
                        JPAExpressions
                                .selectOne()
                                .from(jr)
                                .where(
                                        jr.workerRequest.eq(wr),
                                        jr.jobType.jobTypeId.eq(jobTypeId)
                                )
                                .exists()
                )
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0L : total);
    }

    @Override
    public List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate) {
        Map<Long, MatchingDataDto.WorkerInfo> result = queryFactory
                .select(
                        workerRequest.workerRequestId,
                        worker.id,
                        workerRequest.priorityScore,
                        workerRequest.neighborhoodId,
                        jobType.jobTypeId
                ).from(workerRequest)
                .join(workerRequest.worker, worker)
                .join(workerRequest.jobTypes, workerRequestJobType)
                .join(workerRequestJobType.jobType, jobType)
                .where(
                        workerRequest.requestDate.eq(targetDate),
                        workerRequest.status.eq(RequestStatus.PENDING)
                ).transform(GroupBy.groupBy(workerRequest.workerRequestId).as(
                        Projections.constructor(MatchingDataDto.WorkerInfo.class,
                                workerRequest.workerRequestId,
                                worker.id,
                                workerRequest.priorityScore,
                                workerRequest.neighborhoodId,
                                GroupBy.set(jobType.jobTypeId))

                ));

        return new ArrayList<>(result.values());
    }

    @Override
    public Slice<WorkerRequestDto.WorkerRequestInfoDto> findFilteredWorkerRequests(
            WorkerRequestDto.WorkerRequestFilterDto filterDto,
            Pageable pageable) {

        QWorkerRequest wr = workerRequest;
        QWorker w = worker;
        QWorkerRequestJobType wrjt = workerRequestJobType;
        QJobType jt = jobType;

        JPAQuery<Tuple> query = queryFactory
                .select(
                        wr.workerRequestId,
                        wr.neighborhoodId,
                        wr.requestDate,
                        w.name,
                        w.phoneNumber,
                        jt.name
                )
                .from(wr)
                .join(wr.worker, w)
                .join(wr.jobTypes, wrjt)
                .join(wrjt.jobType, jt)
                .where(
                        wr.status.eq(RequestStatus.PENDING),
                        targetDatesCondition(filterDto.getTargetDates()),
                        neighborhoodCondition(filterDto.getNeighborhoodIds()),
                        jobTypeCondition(filterDto.getJobTypeIds())
                )
                .orderBy(wr.priorityScore.desc());

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize() + 1);

        List<Tuple> result = query.fetch();

        boolean hasNext = false;
        if (result.size() > pageable.getPageSize()) {
            result = result.subList(0, pageable.getPageSize());
            hasNext = true;
        }

        Map<Long, WorkerRequestDto.WorkerRequestInfoDto> resultMap = new LinkedHashMap<>();
        for (Tuple tuple : result) {
            Long requestId = tuple.get(wr.workerRequestId);
            String jobTypeName = tuple.get(jt.name);

            WorkerRequestDto.WorkerRequestInfoDto dto = resultMap.computeIfAbsent(requestId, k ->
                    WorkerRequestDto.WorkerRequestInfoDto.builder()
                            .workerRequestId(tuple.get(wr.workerRequestId))
                            .neighborhoodId(tuple.get(wr.neighborhoodId))
                            .requestDate(tuple.get(wr.requestDate))
                            .workerName(tuple.get(w.name))
                            .workerPhoneNumber(tuple.get(w.phoneNumber))
                            .jobTypeNames(new ArrayList<>())
                            .build()
            );

            dto.getJobTypeNames().add(jobTypeName);
        }
        List<WorkerRequestDto.WorkerRequestInfoDto> content = new ArrayList<>(resultMap.values());

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression jobTypeCondition(List<Long> jobTypeIds) {
        return CollectionUtils.isEmpty(jobTypeIds) ? null : workerRequestJobType.jobType.jobTypeId.in(jobTypeIds);
    }

    private BooleanExpression neighborhoodCondition(List<Long> neighborhoodIds) {
        return CollectionUtils.isEmpty(neighborhoodIds) ? null : workerRequest.neighborhoodId.in(neighborhoodIds);
    }

    private BooleanExpression targetDatesCondition(List<LocalDate> targetDates) {
        return CollectionUtils.isEmpty(targetDates) ? null : workerRequest.requestDate.in(targetDates);
    }
}