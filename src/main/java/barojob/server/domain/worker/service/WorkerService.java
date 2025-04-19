package barojob.server.domain.worker.service;

import barojob.server.domain.user.repository.UserRepository;
import barojob.server.domain.worker.dto.WorkerDto;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public WorkerDto.CreateResponse createWorker(WorkerDto.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        if (workerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다: " + request.getPhoneNumber());
        }

        Worker worker = request.toEntity(passwordEncoder);
        Worker savedWorker = workerRepository.save(worker);

        return WorkerDto.CreateResponse.builder()
                .userId(savedWorker.getId())
                .build();
    }
}
