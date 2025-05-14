package barojob.server.domain.worker.entity;

import barojob.server.common.type.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@IdClass(WorkerRequestId.class)
@Table(
        name = "worker_requests",
        indexes = {
                @Index(name = "idx_wr_worker_id", columnList = "worker_id"),
                @Index(name = "idx_worker_request_status_priority", columnList = "status, priority_score"),
                @Index(
                        name = "idx_wr_neighborhood_status_date_loc_job_score",
                        columnList = "neighborhood_id, status, request_date, priority_score"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_request_id")
    private Long workerRequestId;

    @Id
    @Column(name = "neighborhood_id")
    private Long neighborhoodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "worker_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Worker worker;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @ColumnDefault("50")
    @Builder.Default
    private Double priorityScore = 50.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @ColumnDefault("'PENDING'")
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @OneToMany(
            mappedBy = "workerRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<WorkerRequestJobType> jobTypes = new ArrayList<>();

    public void addJobType(WorkerRequestJobType jobType) {
        this.jobTypes.add(jobType);
        jobType.setWorkerRequest(this);
    }
}