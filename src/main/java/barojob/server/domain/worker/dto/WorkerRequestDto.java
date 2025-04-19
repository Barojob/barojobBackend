package barojob.server.domain.worker.dto;

import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.entity.WorkerRequestJobType;
import barojob.server.domain.worker.entity.WorkerRequestLocation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class WorkerRequestDto {

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

        public WorkerRequest toEntity(Worker worker, List<Neighborhood> neighborhoods, List<JobType> jobTypes) {
            WorkerRequest request = WorkerRequest.builder()
                    .worker(worker)
                    .requestDate(this.requestDate)
                    .build();

            List<WorkerRequestLocation> locations = neighborhoods.stream()
                    .map(n -> WorkerRequestLocation.builder()
                            .workerRequest(request)
                            .neighborhood(n)
                            .build())
                    .collect(Collectors.toList());

            List<WorkerRequestJobType> reqJobTypes = jobTypes.stream()
                    .map(jt -> WorkerRequestJobType.builder()
                            .workerRequest(request)
                            .jobType(jt)
                            .build())
                    .collect(Collectors.toList());

            request.setLocations(locations);
            request.setJobTypes(reqJobTypes);

            return request;
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateResponse {
        private Long workerRequestId;
    }
}

