package barojob.server.domain.auth.service;

import barojob.server.domain.auth.dto.AuthDto;
import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.repository.EmployerRepository;
import barojob.server.domain.session.entity.UserSession;
import barojob.server.domain.session.repository.CustomRedisSessionRepository;
import barojob.server.domain.session.repository.RedisSessionRepository;
import barojob.server.domain.session.service.SessionService;
import barojob.server.domain.session.state.State;
import barojob.server.domain.sms.service.SmsService;
import barojob.server.domain.user.dto.UserDto;
import barojob.server.domain.user.entity.User;
import barojob.server.domain.user.repository.UserRepository;
import barojob.server.domain.worker.entity.Worker;
import barojob.server.domain.worker.repository.WorkerRepository;
import barojob.server.system.exception.model.ErrorCode;
import barojob.server.system.exception.model.RestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final RedisAuthService redisAuthService;
    private final CustomRedisSessionRepository customRedisSessionRepository;
    private final EmployerRepository employerRepository;
    private final WorkerRepository workerRepository;
    private final RedisSessionRepository redisSessionRepository;
    private final SmsService smsService;

    @Transactional
    //로그인 시, 인증번호 발송 전 존재하는 유저인지 확인
    public boolean isValidUser(AuthDto.SignInVerificateRequest request){
        boolean isFound=false;
        if(request.getRole().equals("Worker")) {
            isFound = workerRepository.existsByPhoneNumber(request.getPhoneNumber());
        }
        else if(request.getRole().equals("Employer")){
            isFound = employerRepository.existsByPhoneNumber(request.getPhoneNumber());
        }
        return isFound;
    }
    @Transactional
    public AuthDto.SessionIdResponse signIn(AuthDto.SignInRequest request) {
        Long userId=null;
        if(request.getRole().equals("Worker")){
            Worker foundWorker=workerRepository.findWorkerByPhoneNumber(request.getPhoneNumber());
            System.out.println(foundWorker.getName());
            userId=foundWorker.getId();
        }
        else if(request.getRole().equals("Employer")){
            Employer foundEmployer=employerRepository.findEmployerByPhoneNumber(request.getPhoneNumber());
            System.out.println(foundEmployer.getName());
            userId=foundEmployer.getId();
        }

        System.out.println("userId: "+userId);

        User found = userRepository.findById(userId)
                .orElseThrow(() -> new RestException(ErrorCode.AUTH_USER_NOT_FOUND));

        UserDto.UserResponse userResponse = UserDto.UserResponse.from(found);

        String sessionId = sessionService.createSessionId();

        if(customRedisSessionRepository.isUserSessionActive(found.getNickname())) {
            return AuthDto.SessionIdResponse.builder()
                    .user(userResponse)
                    .sessionId(customRedisSessionRepository.getSessionIdByNickname(found.getNickname()))
                    .build();
        }

        UserSession userSession = UserSession.builder()
                .nickname(found.getNickname())
                .state(State.LOBBY)
                .sessionId(sessionId)
                .build();

        redisAuthService.saveSession(userSession);

        return AuthDto.SessionIdResponse.builder()
                .user(userResponse)
                .sessionId(sessionId)
                .build();

    }
    @Transactional
    public AuthDto.SignUpResponse signUp(AuthDto.SignUpRequest request, boolean isEmployer) {
        //이메일, 닉네임을 통한 회원 조회 : 존재하는 유저 -> 예외처리
        Optional<User> found = userRepository.findByEmail(request.getEmail());

        if(found.isPresent()) throw new RestException(ErrorCode.GLOBAL_ALREADY_EXIST);

        found = userRepository.findByNickname(request.getNickname());

        if(found.isPresent()) throw new RestException(ErrorCode.GLOBAL_ALREADY_EXIST);

        //worker, Employer 회원가입 : businessName이 존재하면 Employer 회원가입, 없으면 Worker 회원가입
        User toSave;

        if(isEmployer){
            toSave = Employer.builder()
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .phoneNumber(request.getPhoneNumber())
                    .name(request.getName())
                    .businessName(request.getBusinessName())
                    .build();
        }
        else{
            toSave = Worker.builder()
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .build();
        }

        User saved = userRepository.save(toSave);
        return AuthDto.SignUpResponse.builder()
                .user(UserDto.UserResponse.from(saved))
                .build();
    }

    public void logout(String sessionId) {
        redisAuthService.deleteSessionId(sessionId);
    }
}
