package barojob.server.domain.test;

import barojob.server.domain.worker.dto.WorkerRequestDto;
import barojob.server.domain.worker.entity.WorkerRequest;
import barojob.server.domain.worker.service.WorkerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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


}
