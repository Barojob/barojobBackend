package barojob.server.domain.location.repository;

import barojob.server.domain.location.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
    List<Neighborhood> findByNeighborhoodIdIn(List<Long> neighborhoodIds);
}
