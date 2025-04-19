package barojob.server.domain.employer.dto;

import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.employer.entity.EmployerRequestDetail;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployerRequestDto {


    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotNull(message = "고용주 ID는 필수입니다.")
        private Long employerId;

        @NotEmpty(message = "요청 날짜는 필수입니다.")
        @FutureOrPresent(message = "요청 날짜는 현재 또는 미래여야 합니다.")
        private LocalDate requestDate;

        @NotNull(message = "근무지 동(Neighborhood) ID는 필수입니다.")
        private Long locationNeighborhoodId;

        @NotEmpty(message = "요청 상세 항목은 비어 있을 수 없습니다.")
        @Valid
        private List<CreateDetail> details;

        public EmployerRequest toEntity(Employer employer, Neighborhood neighborhood, Map<Long, JobType> jobTypeMap) {
            EmployerRequest request = EmployerRequest.builder()
                    .employer(employer)
                    .requestDate(this.requestDate)
                    .locationNeighborhood(neighborhood)
                    .build();

            List<EmployerRequestDetail> detailEntities = this.details.stream()
                    .map(detailDto -> detailDto.toEntity(request, jobTypeMap.get(detailDto.getJobTypeId())))
                    .collect(Collectors.toList());

            request.setDetails(detailEntities);

            return request;
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateDetail {
        @NotEmpty(message = "업종 ID는 필수입니다.")
        private Long jobTypeId;

        @NotEmpty(message = "필요 인원수는 필수입니다.")
        @Positive(message = "필요 인원수는 0보다 커야 합니다.")
        private Integer requiredCount;

        public EmployerRequestDetail toEntity(EmployerRequest employerRequest, JobType jobType) {
            return EmployerRequestDetail.builder()
                    .employerRequest(employerRequest)
                    .jobType(jobType)
                    .requiredCount(this.requiredCount)
                    .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateResponse {
        private Long requestId;
    }
}
