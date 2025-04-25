package barojob.server.domain.employer.repository;

import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.QEmployer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmployerRepositoryCustomImpl implements EmployerRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    public Employer findEmployerByPhoneNumber(String phoneNumber) {
        QEmployer employer = QEmployer.employer;

        Employer result = queryFactory
                .selectFrom(employer)  // ← 전체 Worker 엔티티 조회
                .where(employer.phoneNumber.eq(phoneNumber))
                .fetchFirst();  // 여러 명이면 첫 번째만 가져옴

        return result;
    }
}
