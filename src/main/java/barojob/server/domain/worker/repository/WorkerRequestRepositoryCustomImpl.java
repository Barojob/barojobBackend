package barojob.server.domain.worker.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.worker.dto.WorkerRequestDto.ManualMatchingResponse;
import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.QWorkerRequest;
import barojob.server.domain.worker.entity.QWorkerRequestJobType;
import barojob.server.domain.worker.entity.QWorkerRequestLocation;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
}