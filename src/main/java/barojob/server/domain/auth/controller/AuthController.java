package barojob.server.domain.auth.controller;

import barojob.server.domain.auth.dto.AuthDto;
import barojob.server.domain.auth.service.AuthService;
import barojob.server.domain.session.repository.CustomRedisSessionRepository;
import barojob.server.domain.sms.service.SmsService;
import barojob.server.system.exception.model.ErrorCode;
import barojob.server.system.exception.model.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CustomRedisSessionRepository customRedisSessionRepository;
    private final SmsService smsService;

    //checkUser : sign-in 전 존재하는 유저인지 phoneNumber로 확인 후 인증번호 발송
    @PostMapping(value="/checkUser",consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUser(@RequestBody AuthDto.SignInVerificateRequest request){
        boolean isValid= authService.isValidUser(request);
        if(!isValid){
            throw new RestException(ErrorCode.USER_NOT_FOUND);
        }
        else{
            smsService.saveVerificationCode(request.getPhoneNumber());
        }
        return isValid;

    }

    //sign-in : 인증번호 검사 및 로그인
    @PostMapping(value = "/sign-in",consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthDto.SessionIdResponse signIn(@RequestBody AuthDto.SignInRequest request) {
        boolean isValid=smsService.verifyCode(request.getPhoneNumber(),request.getVerificationCode());
        if(!isValid){
            throw new RestException(ErrorCode.AUTH_VERIFICATION_CODE_MISMATCH);
        }
        return authService.signIn(request);
    }

    //sign-up : 회원가입
    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthDto.SignUpResponse signUp(@RequestBody AuthDto.SignUpRequest request) {
        boolean isEmployer = request.getBusinessName()!=null &&!request.getBusinessName().isBlank();
        return authService.signUp(request,isEmployer);
    }
    @PostMapping("/sign-out")
    public void signOut(@RequestHeader String authorization) {
        authService.logout(authorization);
    }

    //email, pw기반 로그인
    @PostMapping(value = "/sign-in-nonsms",consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthDto.SessionIdResponse signInNonSms(@RequestBody AuthDto.SignInRequestNonSms request) {

        return authService.signInNonSms(request);
    }
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestHeader String authorization) {
        if(customRedisSessionRepository.isValidSessionId(authorization)) {
            String nickname = customRedisSessionRepository.getNicknameBySessionId(authorization);
            return ResponseEntity.ok(AuthDto.ValidSessionTestResponse.builder().nickname(nickname).build());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
