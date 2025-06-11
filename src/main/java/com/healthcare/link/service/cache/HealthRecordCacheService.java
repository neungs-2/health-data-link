package com.healthcare.link.service.cache;

import com.healthcare.link.common.redis.RedisHandler;
import com.healthcare.link.dto.cache.DailySummaryResponseListCacheDto;
import com.healthcare.link.dto.cache.MonthlySummaryResponseListCacheDto;
import com.healthcare.link.dto.response.DailySummaryResponseDto;
import com.healthcare.link.dto.response.MonthlySummaryResponseDto;
import com.healthcare.link.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class HealthRecordCacheService {
    private final RedisHandler<DailySummaryResponseListCacheDto> dailySummaryRedisHandler;
    private final RedisHandler<MonthlySummaryResponseListCacheDto> monthlySummaryRedisHandler;
    private final HealthRecordService healthRecordService;

    public List<DailySummaryResponseDto> getDailySummariesWithCache(String recordkey, String timezone, Long userId) {
        String key = getDailyCacheKey(recordkey, timezone, userId);

        // Redis 조회
        Optional<DailySummaryResponseListCacheDto> cacheResult = dailySummaryRedisHandler.get(key);

        // 캐시 히트 -> 리턴
        if (cacheResult.isPresent()) {
            dailySummaryRedisHandler.expire(key, Duration.ofMinutes(5));
            return cacheResult.get().item();
        }

        // 캐시 미스 -> DB 조회
        List<DailySummaryResponseDto> dbResult = healthRecordService.getDailySummaries(recordkey,timezone, userId);

        // Redis 저장; 캐싱 실패 시 그냥 응답
        try {
            DailySummaryResponseListCacheDto cacheDto = new DailySummaryResponseListCacheDto(dbResult);
            dailySummaryRedisHandler.set(key, cacheDto, Duration.ofMinutes(5));
        } catch (Exception ignore) {}

        return dbResult;
    }

    public List<MonthlySummaryResponseDto> getMonthlySummariesWithCache(String recordkey, String timezone, Long userId) {
        String key = getMonthlyCacheKey(recordkey, timezone, userId);

        // Redis 조회
        Optional<MonthlySummaryResponseListCacheDto> cacheResult = monthlySummaryRedisHandler.get(key);

        // 캐시 히트 -> 리턴
        if (cacheResult.isPresent()) {
            monthlySummaryRedisHandler.expire(key, Duration.ofMinutes(5));
            return cacheResult.get().item();
        }

        // 캐시 미스 -> DB 조회
        List<MonthlySummaryResponseDto> dbResult = healthRecordService.getMonthlySummaries(recordkey,timezone, userId);

        // Redis 저장; 캐싱 실패 시 그냥 응답
        try {
            MonthlySummaryResponseListCacheDto cacheDto = new MonthlySummaryResponseListCacheDto(dbResult);
            monthlySummaryRedisHandler.set(key, cacheDto, Duration.ofMinutes(5));
        } catch (Exception ignore) {}

        return dbResult;
    }

    private String getDailyCacheKey(String recordkey, String timezone, Long userId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("cache:daily:")
                .append(recordkey)
                .append(":")
                .append(userId)
                .append(":")
                .append(timezone)
                .toString();
    }

    private String getMonthlyCacheKey(String recordkey, String timezone, Long userId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("cache:monthly:")
                .append(recordkey)
                .append(":")
                .append(userId)
                .append(":")
                .append(timezone)
                .toString();
    }
}
