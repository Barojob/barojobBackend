package barojob.server.domain.employer.repository;

import barojob.server.domain.employer.entity.EmployerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRequestRepository extends JpaRepository<EmployerRequest, Long> {
}
