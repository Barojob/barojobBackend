package barojob.server.domain.sms.repository;

import barojob.server.domain.sms.entity.SmsEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SmsRepositoryImpl implements SmsCustomRepository{
    private final Duration EXPIRATION =Duration.ofMinutes(5);
    //private final RedisTemplate<String,Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public SmsRepositoryImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void saveVerificationCode(SmsEntity smsEntity) {
        stringRedisTemplate.opsForValue().set(
                smsEntity.getPhoneNumber(),
                smsEntity.getVerificationCode(),
                300, TimeUnit.SECONDS
        );
    }

    @Override
    public String findByPhoneNumber(String phoneNumber) {
        return (String) stringRedisTemplate.opsForValue().get(phoneNumber);
    }

    @Override
    public void deleteByPhoneNumber(String phoneNumber) {
        stringRedisTemplate.delete(phoneNumber);
    }

}