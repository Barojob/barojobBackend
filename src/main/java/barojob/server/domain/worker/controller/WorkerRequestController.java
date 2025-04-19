package barojob.server.domain.worker.controller;

import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.service.WorkerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/worker-request")
public class WorkerRequestController {

    private final WorkerRequestService workerRequestService;

    @PostMapping
    public ResponseEntity<WorkerRequestDto.CreateResponse> createWorkerRequest(
            @Valid @RequestBody WorkerRequestDto.CreateRequest request) {
        WorkerRequestDto.CreateResponse workerRequest = workerRequestService.createWorkerRequest(request);

        return ResponseEntity.ok(workerRequest);
    }
}
