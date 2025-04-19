package barojob.server.domain.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class WorkerRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualMatchingResponse {
        private Long workerRequestId;
        private String workerName;
        private String phoneNumber;
        private Double priorityScore;
    }
}
