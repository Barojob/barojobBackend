package barojob.server.system.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    String host;

    @Value("${spring.data.redis.port}")
    int port;

    private final ObjectMapper objectMapper;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }


//    @Bean
//    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//
//        template.setKeySerializer(stringRedisSerializer);
//        template.setHashKeySerializer(stringRedisSerializer);
//        template.setValueSerializer(serializer);
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
}
