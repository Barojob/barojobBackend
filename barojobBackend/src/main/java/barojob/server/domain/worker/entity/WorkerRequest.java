package barojob.server.domain.worker.entity;


import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.common.type.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worker_requests",
        indexes = {
                @Index(name = "idx_wr_date_status_worker", columnList = "request_date, status, worker_id"),
                @Index(name = "idx_wr_worker_id", columnList = "worker_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wr_worker_date", columnNames = {"worker_id", "request_date"})
        })
public class WorkerRequest extends UserStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_request_id")
    private Long workerRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @ColumnDefault("'PENDING'")
    private RequestStatus status = RequestStatus.PENDING;

    @OneToMany(mappedBy = "workerRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkerRequestLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "workerRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkerRequestJobType> jobTypes = new ArrayList<>();

    public void addLocation(WorkerRequestLocation location) {
        this.locations.add(location);
        location.setWorkerRequest(this);
    }

    public void addJobType(WorkerRequestJobType jobType) {
        this.jobTypes.add(jobType);
        jobType.setWorkerRequest(this);
    }
}