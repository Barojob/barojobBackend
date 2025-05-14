package barojob.server.domain.worker.service;

import barojob.server.common.dto.SliceResponseDto;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.jobType.repository.JobTypeRepository;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import barojob.server.domain.test.TestRequestDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.repository.WorkerRepository;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerRequestService {

    private final WorkerRequestRepository workerRequestRepository;
    private final WorkerRepository workerRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final JobTypeRepository jobTypeRepository;

    @Transactional(readOnly = true)
    public Page<WorkerRequestDto.ManualMatchingResponse> getWorkerRequests(TestRequestDto.ManualMatchingRequest manualMatchingRequest) {
        Pageable pageable = PageRequest.of(manualMatchingRequest.getPage(), 10,
                Sort.by(Sort.Direction.DESC, "worker.priorityScore"));

        return workerRequestRepository.findWorkerRequestPageByNeighborhoodAndJobType(manualMatchingRequest.getNeighborhoodId(), manualMatchingRequest.getJobTypeId(), pageable);
    }

    @Transactional
    public WorkerRequestDto.CreateResponse createWorkerRequest(WorkerRequestDto.CreateRequest request) {
        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new EntityNotFoundException("Worker not found with id: " + request.getWorkerId()));
        List<Neighborhood> neighborhoods = neighborhoodRepository.findByNeighborhoodIdIn(request.getNeighborhoodIds());
        List<JobType> jobTypes = jobTypeRepository.findByJobTypeIdIn(request.getJobTypeIds());

        if (workerRequestRepository.existsByWorkerAndRequestDate(worker, request.getRequestDate())) {
            throw new IllegalStateException("Worker already has a request for this date: " + request.getRequestDate());
        }

        List<Long> workerRequestIds = new ArrayList<>();
        for(Neighborhood neighborhood: neighborhoods) {
            WorkerRequest workerRequest = request.toEntity(worker, neighborhood, jobTypes);
            WorkerRequest savedRequest = workerRequestRepository.save(workerRequest);
            workerRequestIds.add(savedRequest.getWorkerRequestId());
        }

        return WorkerRequestDto.CreateResponse.builder()
                .workerRequestIds(workerRequestIds)
                .build();
    }

    @Transactional(readOnly = true)
    public SliceResponseDto<WorkerRequestDto.WorkerRequestInfoDto> getWorkerRequests(
            WorkerRequestDto.WorkerRequestFilterDto filterDto, Pageable pageable
    ) {
        Slice<WorkerRequestDto.WorkerRequestInfoDto> result = workerRequestRepository.findFilteredWorkerRequests(filterDto, pageable);
        return SliceResponseDto.from(result);
    }
}



