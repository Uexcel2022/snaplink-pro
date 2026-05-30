package com.uexcel.snaplinkpro.analytics.controller;

import com.uexcel.snaplinkpro.analytics.dto.AnalyticsResponse;
import com.uexcel.snaplinkpro.analytics.service.AnalyticsService;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UrlRepository urlRepository;

    @GetMapping("/{urlId}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable Long urlId,
            Authentication authentication) {

        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!url.getUser().getEmail().equals(authentication.getName())) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(
                analyticsService.getAnalytics(url)
        );
    }
}