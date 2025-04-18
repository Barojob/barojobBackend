package barojob.server.domain.worker.service;

import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.jobType.repository.JobTypeRepository;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.location.repository.NeighborhoodRepository;
import barojob.server.domain.test.TestRequestDto;
import barojob.server.domain.worker.dto.WorkerDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.repository.WorkerRepository;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        WorkerRequest workerRequest = request.toEntity(worker, neighborhoods, jobTypes);
        WorkerRequest savedRequest = workerRequestRepository.save(workerRequest);

        return WorkerRequestDto.CreateResponse.builder()
                .workerRequestId(savedRequest.getWorkerRequestId())
                .build();
    }
}



