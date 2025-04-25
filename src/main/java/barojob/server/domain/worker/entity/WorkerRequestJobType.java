package barojob.server.domain.worker.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.domain.jobType.entity.JobType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "worker_request_job_types",
        indexes = {
                @Index(
                        name = "idx_wrjt_request_job",
                        columnList = "worker_request_id, neighborhood_id, job_type_id"
                ),
                @Index(
                        name = "idx_wrjt_job_request",
                        columnList = "job_type_id, worker_request_id, neighborhood_id"
                ),
                @Index(name = "idx_wrjt_job_type_id", columnList = "job_type_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wrjt_request_job",
                        columnNames = {"worker_request_id", "neighborhood_id", "job_type_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkerRequestJobType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_request_job_type_id")
    private Long workerRequestJobTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(
                    name = "worker_request_id",
                    referencedColumnName = "worker_request_id",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
            ),
            @JoinColumn(
                    name = "neighborhood_id",
                    referencedColumnName = "neighborhood_id",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
            )
    })
    private WorkerRequest workerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "job_type_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private JobType jobType;
}