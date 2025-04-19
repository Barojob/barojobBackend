package barojob.server.domain.employer.repository;

import barojob.server.domain.employer.entity.EmployerRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRequestDetailRepository extends JpaRepository<EmployerRequestDetail, Long> ,EmployerRequestDetailRepositoryCustom{
}
