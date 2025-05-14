package barojob.server.domain.worker.repository;

import barojob.server.domain.worker.dto.CursorPagingWorkerRequestDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkerRequestRepository extends JpaRepository<WorkerRequest, Long>, WorkerRequestRepositoryCustom {
    boolean existsByWorkerAndRequestDate(Worker worker, LocalDate requestDate);
    List<CursorPagingWorkerRequestDto> findTopRequests(
            List<Long> neighborhoodIds,
            LocalDate requestDate,
            String status,
            int limit,
            Double cursorPriorityScore,
            Long cursorWorkerId
    );

}
