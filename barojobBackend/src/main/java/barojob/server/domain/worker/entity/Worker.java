package barojob.server.domain.worker.entity;
import barojob.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
@Table(name = "workers")
public class Worker extends User {
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "priority_score", precision = 10, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal priorityScore = BigDecimal.ZERO;

    @Column(name = "worker_request")
    @OneToMany(mappedBy = "worker")
    private List<WorkerRequest> workerRequests = new ArrayList<>();

}
