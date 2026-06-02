package com.uexcel.snaplinkpro.service;

import com.uexcel.snaplinkpro.ratelimit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        String ip = "127.0.0.1";
        String key = "rate:" + ip;

        when(zSetOperations.zCard(key)).thenReturn(50L);

        boolean result = rateLimitService.isAllowed(ip);

        assertTrue(result);

        verify(zSetOperations).removeRangeByScore(eq(key), eq(0.0), anyDouble());
        verify(zSetOperations).zCard(key);
        verify(zSetOperations).add(eq(key), anyString(), anyDouble());
        verify(redisTemplate).expire(key, Duration.ofMinutes(2));
    }

    @Test
    void shouldBlockRequestWhenLimitReached() {
        String ip = "127.0.0.1";
        String key = "rate:" + ip;

        when(zSetOperations.zCard(key)).thenReturn(100L);

        boolean result = rateLimitService.isAllowed(ip);

        assertFalse(result);

        verify(zSetOperations).removeRangeByScore(eq(key), eq(0.0), anyDouble());
        verify(zSetOperations).zCard(key);

        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    void shouldAllowRequestWhenRedisCountIsNull() {
        String ip = "127.0.0.1";
        String key = "rate:" + ip;

        when(zSetOperations.zCard(key)).thenReturn(null);

        boolean result = rateLimitService.isAllowed(ip);

        assertTrue(result);

        verify(zSetOperations).add(eq(key), anyString(), anyDouble());
        verify(redisTemplate).expire(key, Duration.ofMinutes(2));
    }

    @Test
    void shouldUseRatePrefixForRedisKey() {
        String ip = "192.168.1.20";
        String expectedKey = "rate:" + ip;

        when(zSetOperations.zCard(expectedKey)).thenReturn(10L);

        rateLimitService.isAllowed(ip);

        verify(zSetOperations).removeRangeByScore(eq(expectedKey), eq(0.0), anyDouble());
        verify(zSetOperations).zCard(expectedKey);
        verify(zSetOperations).add(eq(expectedKey), anyString(), anyDouble());
        verify(redisTemplate).expire(expectedKey, Duration.ofMinutes(2));
    }
}