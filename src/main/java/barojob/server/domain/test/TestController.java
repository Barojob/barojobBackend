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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path targetPath = uploadDir.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
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
