package barojob.server.domain.worker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRequestId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long workerRequestId;
    private Long neighborhoodId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerRequestId)) return false;
        WorkerRequestId that = (WorkerRequestId) o;
        return Objects.equals(workerRequestId, that.workerRequestId) &&
                Objects.equals(neighborhoodId,  that.neighborhoodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerRequestId, neighborhoodId);
    }
}