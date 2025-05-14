package barojob.server.domain.worker.dto;

import barojob.server.domain.worker.entity.WorkerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorPagingWorkerRequestDto {
    private Long workerRequestId;
    private Long neighborhoodId;
    private Long workerId;
    private LocalDate requestDate;
    private Double priorityScore;
    private WorkerRequestStatus status;

    // String으로 status를 받는 생성자
    public CursorPagingWorkerRequestDto(Long workerRequestId, Long neighborhoodId, Long workerId, LocalDate requestDate, Double priorityScore, String status) {
        this.workerRequestId = workerRequestId;
        this.neighborhoodId = neighborhoodId;
        this.workerId = workerId;
        this.requestDate = requestDate;
        this.priorityScore = priorityScore;
        this.status = WorkerRequestStatus.valueOf(status);  // String -> Enum 변환
    }

}
