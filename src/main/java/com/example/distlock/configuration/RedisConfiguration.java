package com.example.distlock.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class RedisConfiguration {
    private static final String LOCK_NAME = "lock";
    private final RedisProperties redisProperties;

    public RedisConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    @Profile(Constants.REDIS_PROFILE)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    @Profile(Constants.REDIS_PROFILE)
    public RedisConnectionFactory redisConnectionFactory(){
        RedisConnectionFactory connectionFactory = null;
        try{
            System.out.println(String.format("Setting up Redis connection to %s:%s", redisProperties.getHost(), redisProperties.getPort()));
            var config =new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
            config.setPassword(redisProperties.getPassword());

            connectionFactory = new LettuceConnectionFactory(config);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Unable to connect to redis at %s:%s");
        }
        return connectionFactory;
    }

    @Bean(destroyMethod = "destroy")
    @Profile(Constants.REDIS_PROFILE)
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, LOCK_NAME);
    }
}
