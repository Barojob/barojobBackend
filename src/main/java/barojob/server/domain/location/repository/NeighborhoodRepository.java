package barojob.server.domain.location.repository;

import barojob.server.domain.location.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
    List<Neighborhood> findByNeighborhoodIdIn(List<Long> neighborhoodIds);
    Optional<Neighborhood> findByNeighborhoodName(String neighborhoodName);
}
