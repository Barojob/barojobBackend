package barojob.server.domain.employer.repository;

import barojob.server.domain.employer.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
}
