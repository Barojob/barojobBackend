package barojob.server.domain.employer.repository;

import barojob.server.domain.employer.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Employer findEmployerByPhoneNumber(String phoneNumber);
    Employer findEmployerByEmail(String email);
}
