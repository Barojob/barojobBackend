package barojob.server.domain.sms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsVerifyRequest {
    private String phoneNumber;
    private String verificationCode;
}