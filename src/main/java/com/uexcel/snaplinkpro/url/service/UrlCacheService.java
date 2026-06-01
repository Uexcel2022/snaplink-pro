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

    public void cacheUrl(
            String shortCode,
            String originalUrl) {

        redisTemplate.opsForValue().set(
                PREFIX + shortCode,
                originalUrl,
                Duration.ofHours(24)
        );
    }

    public String getCachedUrl(String shortCode) {

        return redisTemplate.opsForValue()
                .get(PREFIX + shortCode);
    }
}