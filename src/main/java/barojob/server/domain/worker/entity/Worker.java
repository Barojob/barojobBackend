package barojob.server.domain.worker.entity;
import barojob.server.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("WORKER")
@Table(name = "workers")
public class Worker extends User {
    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Min(0)
    @Max(100)
    @Column(name = "priority_score")
    @ColumnDefault("'50'")
    @Builder.Default
    private Double priorityScore = 50.0;

    @OneToMany(mappedBy = "worker")
    @Builder.Default
    private List<WorkerRequest> workerRequests = new ArrayList<>();

}
