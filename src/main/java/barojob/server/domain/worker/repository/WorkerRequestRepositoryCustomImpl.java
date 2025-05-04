package barojob.server.domain.worker.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.match.dto.MatchingDataDto;
import barojob.server.domain.worker.dto.WorkerRequestDto.ManualMatchingResponse;
import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.QWorkerRequest;
import barojob.server.domain.worker.entity.QWorkerRequestJobType;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

        QWorkerRequest         wr = QWorkerRequest.workerRequest;
        QWorker                w  = QWorker.worker;
        QWorkerRequestJobType  jr = QWorkerRequestJobType.workerRequestJobType;

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
}