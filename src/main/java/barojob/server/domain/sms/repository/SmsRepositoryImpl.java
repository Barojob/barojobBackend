package barojob.server.domain.sms.repository;

import barojob.server.domain.sms.entity.SmsEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SmsRepositoryImpl implements SmsCustomRepository{
    private final Duration EXPIRATION =Duration.ofMinutes(5);
    private final RedisTemplate<String,Object> redisTemplate;

    public SmsRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveVerificationCode(SmsEntity smsEntity) {
        redisTemplate.opsForValue().set(
                smsEntity.getPhoneNumber(),
                smsEntity.getVerificationCode(),
                300, TimeUnit.SECONDS
        );
    }

    @Override
    public String findByPhoneNumber(String phoneNumber) {
        return (String) redisTemplate.opsForValue().get(phoneNumber);
    }

    @Override
    public void deleteByPhoneNumber(String phoneNumber) {
        redisTemplate.delete(phoneNumber);
    }

}