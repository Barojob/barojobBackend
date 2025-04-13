package barojob.server.domain.employer.entity;

import barojob.server.common.timebaseentity.UserStampedEntity;
import barojob.server.common.type.RequestStatus;
import barojob.server.domain.location.entity.Neighborhood;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_neighborhood_id", nullable = false) // DB 컬럼명은 그대로 사용
    private Neighborhood locationNeighborhood;

    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 저장
    @Column(name = "status", length = 20) // Enum 값 길이에 맞게 조정
    @ColumnDefault("'PENDING'") // DB 기본값 설정 (문자열이므로 홑따옴표 사용)
    private RequestStatus status = RequestStatus.PENDING; // Java 객체 기본값

    @OneToMany(mappedBy = "employerRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployerRequestDetail> details = new ArrayList<>();

    public void addDetail(EmployerRequestDetail detail) {
        this.details.add(detail);
        detail.setEmployerRequest(this);
    }
}