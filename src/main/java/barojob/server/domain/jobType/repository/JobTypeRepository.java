package barojob.server.domain.jobType.repository;

import barojob.server.domain.jobType.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    List<JobType> findByJobTypeIdIn(List<Long> jobTypeIds);
    Optional<JobType> findByName(String name);
}
