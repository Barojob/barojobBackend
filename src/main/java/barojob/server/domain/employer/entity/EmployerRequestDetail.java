package barojob.server.domain.employer.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.match.entity.Match;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employer_request_details",
        indexes = {
                @Index(name = "idx_erd_reqid_jobtype", columnList = "request_id, job_type_id"),
                @Index(name = "idx_erd_job_type_id", columnList = "job_type_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_erd_request_job", columnNames = {"request_id", "job_type_id"})
        })
public class EmployerRequestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_detail_id")
    private Long requestDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private EmployerRequest employerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_type_id", nullable = false)
    private JobType jobType;

    @OneToMany(mappedBy = "employerRequestDetail", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Column(name = "matched_count")
    @Builder.Default
    private int matchedCount = 0;

}