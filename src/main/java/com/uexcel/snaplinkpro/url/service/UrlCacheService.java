package com.uexcel.snaplinkpro.url.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "url:";
    private static final String CLICK_PREFIX = "clicks:";

    public void cacheUrl(String shortCode, String originalUrl) {
        redisTemplate.opsForValue().set(PREFIX + shortCode, originalUrl);
    }

    public String getCachedUrl(String shortCode) {
        return redisTemplate.opsForValue().get(PREFIX + shortCode);
    }

    public void incrementClick(String shortCode) {
        redisTemplate.opsForValue().increment(CLICK_PREFIX + shortCode);
    }

    public Long getClicks(String shortCode) {
        String value = redisTemplate.opsForValue().get(CLICK_PREFIX + shortCode);
        return value == null ? 0L : Long.parseLong(value);
    }

    // ✅ ADD THIS
    public void resetClicks(String shortCode) {
        redisTemplate.delete(CLICK_PREFIX + shortCode);
    }

    public boolean acquireLock(String key) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent("lock:" + key, "1", Duration.ofMinutes(1));
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        redisTemplate.delete("lock:" + key);
    }
}