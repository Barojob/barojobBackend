package barojob.server.domain.worker.controller;

import barojob.server.domain.worker.dto.CursorPagingWorkerRequestDto;
import barojob.server.domain.worker.dto.JTCursorPagingWorkerRequestDto;
import barojob.server.domain.worker.service.WorkerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/worker-request")
public class WorkerRequestController {

    private final WorkerRequestService workerRequestService;

    @GetMapping("/paged")
    public List<CursorPagingWorkerRequestDto> getWorkerRequests(
            @RequestParam List<Long> neighborhoodIds,
            @RequestParam String requestDate,
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double cursorPriorityScore,
            @RequestParam(required = false) Long cursorWorkerId
    ) {
        return workerRequestService.getTopWorkerRequests(
                neighborhoodIds,
                LocalDate.parse(requestDate),
                status,
                size,
                cursorPriorityScore,
                cursorWorkerId
        );
    }
    @GetMapping("/pagedJT")
    public List<JTCursorPagingWorkerRequestDto> getWorkerRequestsJT(
            @RequestParam List<Long> neighborhoodIds,
            @RequestParam List<String> requestDates,
            @RequestParam(defaultValue = "PENDING") List<String> status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double cursorPriorityScore,
            @RequestParam(required = false) Long cursorWorkerId,
            @RequestParam List<Long> jobTypeIds
    ) {
        List<LocalDate> parsedDates = requestDates.stream()
                .map(LocalDate::parse)
                .collect(Collectors.toList());

        return workerRequestService.getTopWorkerRequestsJT(
                neighborhoodIds,
                parsedDates,
                status,
                size,
                cursorPriorityScore,
                cursorWorkerId,
                jobTypeIds
        );
    }
    //    @PostMapping
    //    public ResponseEntity<WorkerRequestDto.CreateResponse> createWorkerRequest(
    //            @Valid @RequestBody WorkerRequestDto.CreateRequest request) {
    //        WorkerRequestDto.CreateResponse workerRequest = workerRequestService.createWorkerRequest(request);
    //
    //        return ResponseEntity.ok(workerRequest);
    //    }


}
