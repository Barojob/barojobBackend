import barojob.server.domain.match.dto.MatchingDataDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.WorkerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface WorkerRequestRepositoryCustom {
    Page<WorkerRequestDto.ManualMatchingResponse> findWorkerRequestPageByNeighborhoodAndJobType(
            Long neighborhoodId,
            Long jobTypeId,
            Pageable pageable
    );
  
      List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate);
}

