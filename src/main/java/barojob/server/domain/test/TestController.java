package barojob.server.domain.test;

import barojob.server.common.dto.SliceResponseDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.service.WorkerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final WorkerRequestService workerRequestService;

    @PostMapping("/manualMatching")
    public Page<WorkerRequestDto.ManualMatchingResponse> getWorkerRequests(@RequestBody TestRequestDto.ManualMatchingRequest manualMatchingRequest) {
        Page<WorkerRequestDto.ManualMatchingResponse> responses = workerRequestService.getWorkerRequests(manualMatchingRequest);
        System.out.println("메서드가 실행됐다네");
        return responses;
    }

    @GetMapping("/worker-requests")
    public ResponseEntity<SliceResponseDto<WorkerRequestDto.WorkerRequestInfoDto>> getWorkerRequests(
            @RequestParam(required = false) List<Long> neighborhoodIds,
            @RequestParam(required = false) List<Long> jobTypeIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> targetDates,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        WorkerRequestDto.WorkerRequestFilterDto filterDto = WorkerRequestDto.WorkerRequestFilterDto.builder()
                .neighborhoodIds(neighborhoodIds)
                .jobTypeIds(jobTypeIds)
                .targetDates(targetDates)
                .build();

        SliceResponseDto<WorkerRequestDto.WorkerRequestInfoDto> result = workerRequestService.getWorkerRequests(filterDto, pageable);

        return ResponseEntity.ok(result);
    }
}
