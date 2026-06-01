package com.uexcel.snaplinkpro.url.controller;

import com.uexcel.snaplinkpro.analytics.event.UrlClickEvent;
import com.uexcel.snaplinkpro.analytics.service.AnalyticsService;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import com.uexcel.snaplinkpro.url.service.UrlCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String originalUrl = urlCacheService.getCachedUrl(shortCode);

        if (originalUrl == null) {
            Url url = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new RuntimeException("Short URL not found"));

            originalUrl = url.getOriginalUrl();
            urlCacheService.cacheUrl(shortCode, originalUrl);
        }

        // 👉 FIRE AND FORGET EVENT
        applicationEventPublisher.publishEvent(
                new UrlClickEvent(
                        shortCode,
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        request.getHeader("Referer")
                )
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}