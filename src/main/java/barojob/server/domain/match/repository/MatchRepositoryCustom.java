package barojob.server.domain.match.repository;

import java.time.LocalDate;
import java.util.Set;

public interface MatchRepositoryCustom {
    Set<Long> findMatchedWorkerIdsByDate(LocalDate targetDate);
}
