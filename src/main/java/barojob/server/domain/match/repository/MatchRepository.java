package barojob.server.domain.match.repository;

import barojob.server.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MatchRepository extends JpaRepository<Match, Long> {
    long countByMatchDatetime(LocalDateTime matchDatetime);
}
