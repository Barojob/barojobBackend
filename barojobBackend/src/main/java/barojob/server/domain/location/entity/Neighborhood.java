package barojob.server.domain.location.entity;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.worker.entity.WorkerRequestLocation;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "neighborhoods")
public class Neighborhood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "neighborhood_id")
    private Integer neighborhoodId;

    @Column(name = "neighborhood_name", nullable = false, length = 100)
    private String neighborhoodName;

    @OneToMany(mappedBy = "locationNeighborhood", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployerRequest> employerRequests = new ArrayList<>();

    @OneToMany(mappedBy = "neighborhood", fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkerRequestLocation> workerRequestLocations = new ArrayList<>();
}
