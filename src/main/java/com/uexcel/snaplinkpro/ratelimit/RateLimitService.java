package com.uexcel.snaplinkpro.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String RATE_PREFIX = "rate:";

    private static final int LIMIT = 100;
    private static final long WINDOW = 60_000; // 1 minute

    public boolean isAllowed(String ip) {

        String key = RATE_PREFIX + ip;
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW;

        // 1. Remove old requests outside window
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 2. Count current requests in window
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= LIMIT) {
            return false;
        }

        // 3. Add current request timestamp
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);

        // 4. Set expiry to avoid memory leak
        redisTemplate.expire(key, Duration.ofMinutes(2));

        return true;
    }
}
