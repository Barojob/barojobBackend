package barojob.server.domain.employer.service;

import barojob.server.domain.employer.dto.EmployerDto;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.repository.EmployerRepository;
import barojob.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployerDto.CreateResponse createEmployer(EmployerDto.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        if (employerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다: " + request.getPhoneNumber());
        }

        Employer employer = request.toEntity(passwordEncoder);
        Employer savedEmployer = employerRepository.save(employer);

        return EmployerDto.CreateResponse.builder()
                .userId(savedEmployer.getId())
                .build();
    }
}
