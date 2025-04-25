package barojob.server.domain.worker.repository;

import barojob.server.domain.worker.entity.QWorker;
import barojob.server.domain.worker.entity.Worker;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class WorkerRepositoryCustomImpl implements WorkerRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    public Worker findWorkerByPhoneNumber(String phoneNumber) {
        QWorker worker = QWorker.worker;

        Worker result = queryFactory
                .selectFrom(worker)  // ← 전체 Worker 엔티티 조회
                .where(worker.phoneNumber.eq(phoneNumber))
                .fetchFirst();  // 여러 명이면 첫 번째만 가져옴

        return result;
    }
}
