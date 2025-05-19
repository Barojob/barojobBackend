package barojob.server.domain.worker.repository;

import barojob.server.domain.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Worker findWorkerByPhoneNumber(String phoneNumber);
    Worker findWorkerByEmail(String email);
}
