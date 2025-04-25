package barojob.server.domain.match.entity;

import barojob.server.common.timebaseentity.TimeStampedEntity;
import barojob.server.domain.employer.entity.EmployerRequestDetail;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches",
        indexes = {
                @Index(name = "idx_matches_detail_worker", columnList = "employer_request_detail_id, worker_id"),
                @Index(name = "idx_matches_worker_date", columnList = "worker_id, match_datetime"),
                @Index(name = "idx_matches_date_time_erd", columnList = "match_datetime, employer_request_detail_id"),
                @Index(name = "idx_matches_date_time_wr", columnList = "match_datetime, worker_request_id"),
                @Index(name = "idx_matches_erd_id", columnList = "employer_request_detail_id"),
                @Index(name = "idx_matches_wr_id", columnList = "worker_request_id"),
                @Index(name = "idx_matches_worker_id", columnList = "worker_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_match_worker_date", columnNames = {"worker_id", "match_datetime"})
        })
public class Match extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_request_detail_id", nullable = false)
    private EmployerRequestDetail employerRequestDetail;

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
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(name = "match_datetime", nullable = false)
    private LocalDateTime matchDatetime;
}