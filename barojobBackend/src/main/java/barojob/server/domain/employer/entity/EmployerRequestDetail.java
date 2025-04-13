package barojob.server.domain.employer.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.match.entity.Match;
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
@Table(name = "employer_request_details",
        indexes = {
                @Index(name = "idx_erd_reqid_jobtype", columnList = "request_id, job_type_id"),
                @Index(name = "idx_erd_job_type_id", columnList = "job_type_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_erd_request_job", columnNames = {"request_id", "job_type_id"})
        })
public class EmployerRequestDetail extends UserStampedEntity {

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

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @OneToMany(mappedBy = "employerRequestDetail", fetch = FetchType.LAZY) // Match 엔티티의 employerRequestDetail 필드에 의해 매핑됨
    @Builder.Default // Builder 사용 시 초기화
    private List<Match> matches = new ArrayList<>();

    @Column(name = "matched_count")
    private int matchedCount = 0; // Java 기본값 설정

}