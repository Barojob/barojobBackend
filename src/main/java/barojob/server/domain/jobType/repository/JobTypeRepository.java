package barojob.server.domain.jobType.repository;

import barojob.server.domain.jobType.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    List<JobType> findByJobTypeIdIn(List<Long> jobTypeIds);
}
