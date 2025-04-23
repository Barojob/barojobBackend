package barojob.server.domain.sms.controller;

import barojob.server.domain.sms.dto.SmsRequest;
import barojob.server.domain.sms.dto.SmsVerifyRequest;
import barojob.server.domain.sms.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {
    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    //인증번호 발송
    @PostMapping("/send")
    public ResponseEntity<String> saveCode(@RequestBody SmsRequest smsRequest){
        System.out.println("post, sms/send");
        smsService.saveVerificationCode(smsRequest.getPhoneNumber().replace("-",""));
        return ResponseEntity.ok("인증번호 저장 완료");
    }
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyCode(@RequestBody SmsVerifyRequest verifyRequest){
        System.out.println("post, sms/verify");
        String phoneNumber= verifyRequest.getPhoneNumber().replace("-","");
        String verificationCode= verifyRequest.getVerificationCode();

        boolean isValid =smsService.verifyCode(phoneNumber,verificationCode);
        return ResponseEntity.ok(isValid);
    }


}

