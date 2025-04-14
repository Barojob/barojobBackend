package barojob.server.domain.employer.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.common.type.RequestStatus;
import barojob.server.domain.location.entity.Neighborhood;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employer_requests",
        indexes = {
                @Index(name = "idx_er_date_status_loc", columnList = "request_date, status, location_neighborhood_id"),
                @Index(name = "idx_er_employer_id", columnList = "employer_id"),
                @Index(name = "idx_er_location_neighborhood_id", columnList = "location_neighborhood_id"),
                @Index(name = "idx_er_status", columnList = "status")}
)
public class EmployerRequest extends UserStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    private LocalDate requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_neighborhood_id", nullable = false)
    private Neighborhood locationNeighborhood;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @ColumnDefault("'PENDING'")
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @OneToMany(mappedBy = "employerRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployerRequestDetail> details = new ArrayList<>();

    public void addDetail(EmployerRequestDetail detail) {
        this.details.add(detail);
        detail.setEmployerRequest(this);
    }
}