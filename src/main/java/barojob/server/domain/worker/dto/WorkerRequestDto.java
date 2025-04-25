package barojob.server.domain.worker.dto;

import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.entity.WorkerRequestJobType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull(message = "근로자 ID는 필수입니다.")
        private Long workerId;

        @NotEmpty(message = "요청 날짜는 필수입니다.")
        @FutureOrPresent(message = "요청 날짜는 현재 또는 미래여야 합니다.")
        private LocalDate requestDate;

        @NotEmpty(message = "희망 근무지 ID 목록은 비어 있을 수 없습니다.")
        private List<Long> neighborhoodIds;

        @NotEmpty(message = "가능 업종 ID 목록은 비어 있을 수 없습니다.")
        private List<Long> jobTypeIds;

        public WorkerRequest toEntity(Worker worker,
                                      Neighborhood neighborhood,
                                      List<JobType> jobTypes) {
            WorkerRequest request = WorkerRequest.builder()
                    .worker(worker)
                    .requestDate(this.requestDate)
                    .neighborhood(neighborhood)
                    .priorityScore(worker.getPriorityScore())
                    .neighborhoodId(neighborhood.getNeighborhoodId())
                    .build();

            List<WorkerRequestJobType> reqJobTypes = jobTypes.stream()
                    .map(jt -> WorkerRequestJobType.builder()
                            .workerRequest(request)
                            .jobType(jt)
                            .build())
                    .collect(Collectors.toList());

            request.setJobTypes(reqJobTypes);
            return request;
        }

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class CreateResponse {
            private List<Long> workerRequestIds;
        }
    }
}

