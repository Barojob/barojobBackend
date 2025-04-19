package barojob.server.domain.employer.repository;

import barojob.server.domain.match.dto.MatchingDataDto;

import java.time.LocalDate;
import java.util.List;

public interface EmployerRequestDetailRepositoryCustom {
    List<MatchingDataDto.EmployerDetailInfo> findEligibleDetailInfoForMatching(LocalDate targetDate);
}
