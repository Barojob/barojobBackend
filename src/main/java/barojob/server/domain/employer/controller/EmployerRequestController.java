package barojob.server.domain.employer.controller;

import barojob.server.domain.employer.dto.EmployerRequestDto;
import barojob.server.domain.employer.service.EmployerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employer-request")
public class EmployerRequestController {

    private final EmployerRequestService employerRequestService;

    @PostMapping
    public ResponseEntity<EmployerRequestDto.CreateResponse> createWorkerRequest(
            @Valid @RequestBody EmployerRequestDto.CreateRequest request) {
        EmployerRequestDto.CreateResponse employerRequest = employerRequestService.createEmployerRequest(request);

        return ResponseEntity.ok(employerRequest);
    }

}
