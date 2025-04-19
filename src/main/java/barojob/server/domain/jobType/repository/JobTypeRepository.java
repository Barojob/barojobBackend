package barojob.server.domain.jobType.repository;

import barojob.server.domain.jobType.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    List<JobType> findByJobTypeIdIn(List<Long> jobTypeIds);
    Optional<JobType> findByName(String name);
}
