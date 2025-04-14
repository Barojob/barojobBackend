package barojob.server.domain.worker.repository;

import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WorkerRequestRepository extends JpaRepository<WorkerRequest, Long> {
    boolean existsByWorkerAndRequestDate(Worker worker, LocalDate requestDate);
}
