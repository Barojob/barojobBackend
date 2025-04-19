package barojob.server.domain.employer.repository;

import barojob.server.common.type.RequestStatus;
import barojob.server.domain.match.dto.MatchingDataDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static barojob.server.domain.employer.entity.QEmployer.employer;
import static barojob.server.domain.employer.entity.QEmployerRequest.employerRequest;
import static barojob.server.domain.employer.entity.QEmployerRequestDetail.employerRequestDetail;
import static barojob.server.domain.jobType.entity.QJobType.jobType;
import static barojob.server.domain.location.entity.QNeighborhood.neighborhood;

@Repository
@RequiredArgsConstructor
public class EmployerRequestDetailRepositoryImpl implements EmployerRequestDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /*
        타켓 날짜에 매칭 대상이 되는 업주의 요청 선별
     */
    @Override
    public List<MatchingDataDto.EmployerDetailInfo> findEligibleDetailInfoForMatching(LocalDate targetDate) {
        return queryFactory
                .select(Projections.constructor(MatchingDataDto.EmployerDetailInfo.class,
                        employerRequestDetail.requestDetailId,
                        employerRequest.requestId,
                        employer.id,
                        employerRequestDetail.requiredCount.subtract(employerRequestDetail.matchedCount),
                        neighborhood.neighborhoodId,
                        jobType.jobTypeId
                ))
                .from(employerRequestDetail)
                .join(employerRequestDetail.employerRequest, employerRequest)
                .join(employerRequest.employer, employer)
                .join(employerRequest.locationNeighborhood, neighborhood)
                .join(employerRequestDetail.jobType, jobType)
                .where(
                        employerRequest.requestDate.eq(targetDate),
                        employerRequest.status.in(RequestStatus.PENDING, RequestStatus.PARTIALLY_MATCHED),
                        employerRequestDetail.requiredCount.gt(employerRequestDetail.matchedCount)
                )
                .fetch();
    }
}