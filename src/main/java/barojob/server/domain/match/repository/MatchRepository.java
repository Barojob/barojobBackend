package barojob.server.domain.match.repository;

import barojob.server.domain.match.entity.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long>, MatchRepositoryCustom {
    long countByMatchDatetime(LocalDateTime matchDatetime);

    @EntityGraph(attributePaths = {"worker",
            "employerRequestDetail.employerRequest.employer",
            "employerRequestDetail.employerRequest.locationNeighborhood",
            "employerRequestDetail.jobType"})
    List<Match> findDetailByMatchDatetime(@Param("matchTime") LocalDateTime matchTime);
}
