package com.healthcare.link.service.cache;

import com.healthcare.link.common.redis.RedisHandler;
import com.healthcare.link.dto.cache.DailySummaryResponseListCacheDto;
import com.healthcare.link.dto.cache.MonthlySummaryResponseListCacheDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto;
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
    private final RedisHandler<String> stringRedisHandler;
    private final HealthRecordService healthRecordService;

    public List<DailySummaryResponseDto> getDailySummariesWithCache(String recordkey, String timezone, Long userId) {
        String key = getDailyCacheKey(recordkey, timezone, userId);

        // Redis 조회
        Optional<DailySummaryResponseListCacheDto> cacheResult = dailySummaryRedisHandler.get(key);

        // 캐시 히트 -> ttl 리셋, 리턴
        if (cacheResult.isPresent()) {
            dailySummaryRedisHandler.expire(key, Duration.ofMinutes(5));
            return cacheResult.get().item();
        }

        // 캐시 미스 -> DB 조회
        List<DailySummaryResponseDto> dbResult = healthRecordService.getDailySummaries(recordkey,timezone, userId);

        // Redis 저장
        // 캐시 저장 실패 시 그냥 응답
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

        // 캐시 히트 -> ttl 리셋, 리턴
        if (cacheResult.isPresent()) {
            monthlySummaryRedisHandler.expire(key, Duration.ofMinutes(5));
            return cacheResult.get().item();
        }

        // 캐시 미스 -> DB 조회
        List<MonthlySummaryResponseDto> dbResult = healthRecordService.getMonthlySummaries(recordkey,timezone, userId);

        // Redis 저장
        // 캐시 저장 실패 시 그냥 응답
        try {
            MonthlySummaryResponseListCacheDto cacheDto = new MonthlySummaryResponseListCacheDto(dbResult);
            monthlySummaryRedisHandler.set(key, cacheDto, Duration.ofMinutes(5));
        } catch (Exception ignore) {}

        return dbResult;
    }

    public void saveStepsWithIdempotentCheck(StepsRecordRequestDto request, Long userId) {
        String key = getIdempotentKey(request.recordkey(), userId);
        boolean hasNotIdempotentKey = stringRedisHandler.setNx(key, "LOCKED", Duration.ofMinutes(5));

        // 멱등성 키 없는 경우 (키 저장 성공) Steps 데이터 저장
        if (hasNotIdempotentKey) {
            healthRecordService.saveSteps(request, userId);
        }
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

    private String getIdempotentKey(String recordkey, Long userId) {
        StringBuilder sb = new StringBuilder();
        return sb.append("idempotent:")
                .append(recordkey)
                .append(":")
                .append(userId)
                .toString();
    }
}
