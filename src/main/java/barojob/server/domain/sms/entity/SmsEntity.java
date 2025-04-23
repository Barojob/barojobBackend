package barojob.server.domain.sms.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value="sms",timeToLive=300)
public class SmsEntity implements Serializable {
    @Id
    private String phoneNumber;

    private String VerificationCode;
}
