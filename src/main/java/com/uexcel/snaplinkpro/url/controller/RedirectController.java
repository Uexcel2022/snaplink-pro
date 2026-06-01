package com.uexcel.snaplinkpro.url.controller;

import com.uexcel.snaplinkpro.analytics.event.UrlClickEvent;
import com.uexcel.snaplinkpro.exception.UrlNotFoundException;
import com.uexcel.snaplinkpro.ratelimit.RateLimitService;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import com.uexcel.snaplinkpro.url.service.UrlCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RateLimitService rateLimitService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String ip = getClientIp(request);

        if (!rateLimitService.isAllowed(ip)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }

        // 1. Get URL from cache
        String originalUrl = urlCacheService.getCachedUrl(shortCode);

        if (originalUrl == null) {
            Url url = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(
                            "Short URL not found",HttpStatus.NOT_FOUND));

            originalUrl = url.getOriginalUrl();

            urlCacheService.cacheUrl(shortCode, originalUrl);
        }

        // 2. FAST click counter (Redis only)
        urlCacheService.incrementClick(shortCode);

        // 3. Async analytics event
        applicationEventPublisher.publishEvent(
                new UrlClickEvent(
                        shortCode,
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        request.getHeader("Referer")
                )
        );

        // 4. Redirect immediately
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}