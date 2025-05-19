package barojob.server.domain.worker.dto;

import barojob.server.domain.worker.entity.WorkerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JTCursorPagingWorkerRequestDto {
    private Long workerRequestId;
    private Long neighborhoodId;
    private Long workerId;
    private Long jobTypeId;
    private String jobTypeName;
    private LocalDate requestDate;
    private Double priorityScore;
    private WorkerRequestStatus status;

    public JTCursorPagingWorkerRequestDto(Long workerRequestId, Long neighborhoodId, Long workerId, LocalDate requestDate, Double priorityScore, String status, Long jobTypeId
            ,String jobTypeName
    ) {
        this.workerRequestId = workerRequestId;
        this.neighborhoodId = neighborhoodId;
        this.workerId = workerId;
        this.requestDate = requestDate;
        this.priorityScore = priorityScore;
        this.status = WorkerRequestStatus.valueOf(status);  // String -> Enum 변환
        this.jobTypeId=jobTypeId;
        this.jobTypeName=jobTypeName;
    }
}
