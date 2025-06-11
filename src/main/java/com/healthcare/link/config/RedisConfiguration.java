package com.healthcare.link.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.link.common.redis.RedisHandler;
import com.healthcare.link.dto.cache.DailySummaryResponseListCacheDto;
import com.healthcare.link.dto.cache.MonthlySummaryResponseListCacheDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(host, port)
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, DailySummaryResponseListCacheDto> dailySummaryRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, DailySummaryResponseListCacheDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<DailySummaryResponseListCacheDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, DailySummaryResponseListCacheDto.class);
        template.setValueSerializer(serializer);

        return template;
    }

    @Bean
    public RedisTemplate<String, MonthlySummaryResponseListCacheDto> monthlySummaryRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, MonthlySummaryResponseListCacheDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<MonthlySummaryResponseListCacheDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, MonthlySummaryResponseListCacheDto.class);
        template.setValueSerializer(serializer);

        return template;
    }

    @Bean
    public RedisHandler<DailySummaryResponseListCacheDto> dailySummaryRedisHandler(RedisTemplate<String, DailySummaryResponseListCacheDto> dailySummaryRedisTemplate) {
        return new RedisHandler<>(dailySummaryRedisTemplate);
    }

    @Bean
    public RedisHandler<MonthlySummaryResponseListCacheDto> monthlySummaryRedisHandler(RedisTemplate<String, MonthlySummaryResponseListCacheDto> monthlySummaryRedisTemplate) {
        return new RedisHandler<>(monthlySummaryRedisTemplate);
    }
}
