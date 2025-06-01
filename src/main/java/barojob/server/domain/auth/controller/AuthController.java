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

    @PostMapping(value="/checkUser",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public boolean checkUser(@RequestParam String phoneNumber,@RequestParam String role){
        AuthDto.SignInVerificateRequest request=AuthDto.SignInVerificateRequest.of(phoneNumber, role);
        boolean isValid= authService.isValidUser(request);
        if(!isValid){
            throw new RestException(ErrorCode.USER_NOT_FOUND);
        }
        else{
            smsService.saveVerificationCode(phoneNumber);
        }
        return isValid;

    }
    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public AuthDto.SessionIdResponse signIn(@RequestParam String verificationCode,@RequestParam String phoneNumber,@RequestParam String role) {
        boolean isValid = smsService.verifyCode(phoneNumber,verificationCode);
        if(!isValid){
            System.out.println("Wrong verificationCode");
            throw new RestException(ErrorCode.AUTH_VERIFICATION_CODE_MISMATCH);

        }

        AuthDto.SignInRequest request = AuthDto.SignInRequest.of(verificationCode,phoneNumber,role);
        return authService.signIn(request);
    }

    //회원가입 : 유태민
    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public AuthDto.SignUpResponse signUp(@RequestParam String email,
                                               @RequestParam String nickname,
                                               @RequestParam String password,
                                               @RequestParam String phoneNumber,
                                               @RequestParam String name,
                                                @RequestParam(required = false) String businessName) {
        boolean isEmployer = businessName!=null &&!businessName.isBlank();

        AuthDto.SignUpRequest request = AuthDto.SignUpRequest.of(email, nickname, password,phoneNumber,name,businessName);
        return authService.signUp(request,isEmployer);

    }

    @PostMapping("/sign-out")
    public void signOut(@RequestHeader String authorization) {
        authService.logout(authorization);
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
