package barojob.server.domain.worker.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.domain.location.entity.Neighborhood;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worker_request_locations",
        indexes = {
                @Index(name = "idx_wrl_request_neighborhood", columnList = "worker_request_id, neighborhood_id"),
                @Index(name = "idx_wrl_neighborhood_request", columnList = "neighborhood_id, worker_request_id"),
                @Index(name = "idx_wrl_neighborhood_id", columnList = "neighborhood_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wrl_request_neighborhood", columnNames = {"worker_request_id", "neighborhood_id"})
        })
public class WorkerRequestLocation extends UserStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_request_location_id")
    private Long workerRequestLocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_request_id", nullable = false)
    private WorkerRequest workerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;
}
