package barojob.server.domain.match.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static barojob.server.domain.match.entity.QMatch.match;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findMatchedWorkerIdsByDate(LocalDate targetDate) {
        List<Long> ids = queryFactory
                .select(match.worker.id)
                .from(match)
                .where(match.matchDatetime.goe(targetDate.atStartOfDay())
                        .and(match.matchDatetime.lt(targetDate.plusDays(1).atStartOfDay())))
                .fetch();
        return new HashSet<>(ids);
    }
}
