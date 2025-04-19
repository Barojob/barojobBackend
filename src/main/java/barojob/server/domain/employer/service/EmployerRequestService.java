package barojob.server.domain.employer.service;

import barojob.server.domain.employer.dto.EmployerRequestDto;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.employer.repository.EmployerRepository;
import barojob.server.domain.employer.repository.EmployerRequestRepository;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.jobType.repository.JobTypeRepository;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerRequestService {
    private final EmployerRepository employerRepository;
    private final EmployerRequestRepository employerRequestRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final JobTypeRepository jobTypeRepository;

    @Transactional
    public EmployerRequestDto.CreateResponse createEmployerRequest(EmployerRequestDto.CreateRequest request) {
        Employer employer = employerRepository.findById(request.getEmployerId())
                .orElseThrow(() -> new EntityNotFoundException("Employer not found with id: " + request.getEmployerId()));
        Neighborhood neighborhood = neighborhoodRepository.findById(request.getLocationNeighborhoodId())
                .orElseThrow(() -> new EntityNotFoundException("Neighborhood not found with id: " + request.getLocationNeighborhoodId()));

        List<Long> jobTypeIds = request.getDetails().stream()
                .map(EmployerRequestDto.CreateDetail::getJobTypeId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, JobType> jobTypeMap = jobTypeRepository.findByJobTypeIdIn(jobTypeIds).stream()
                .collect(Collectors.toMap(JobType::getJobTypeId, Function.identity()));

        EmployerRequest employerRequest = request.toEntity(employer, neighborhood, jobTypeMap);
        EmployerRequest savedRequest = employerRequestRepository.save(employerRequest);

        return EmployerRequestDto.CreateResponse.builder()
                .requestId(savedRequest.getRequestId())
                .build();
    }
}
