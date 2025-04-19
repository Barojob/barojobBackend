package barojob.server.domain.worker.repository;

import barojob.server.domain.match.dto.MatchingDataDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface WorkerRequestRepositoryCustom {
    List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate);
}