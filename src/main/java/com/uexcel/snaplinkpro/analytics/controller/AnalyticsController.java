package com.uexcel.snaplinkpro.analytics.controller;

import com.uexcel.snaplinkpro.analytics.dto.AnalyticsResponse;
import com.uexcel.snaplinkpro.analytics.dto.ClickTrendResponse;
import com.uexcel.snaplinkpro.analytics.dto.TopUrlResponse;
import com.uexcel.snaplinkpro.analytics.service.AnalyticsService;
import com.uexcel.snaplinkpro.exception.AccessDeniedException;
import com.uexcel.snaplinkpro.exception.ForbiddenException;
import com.uexcel.snaplinkpro.exception.UrlNotFoundException;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        Url url = getUserOwnedUrl(urlId, authentication);

        return ResponseEntity.ok(
                analyticsService.getAnalytics(url)
        );
    }

    @GetMapping("/top-urls")
    public ResponseEntity<List<TopUrlResponse>> getTopUrls(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        return ResponseEntity.ok(
                analyticsService.getTopUrls(authentication.getName(), limit)
        );
    }

//    @GetMapping("/top-urls")
//    public ResponseEntity<List<TopUrlResponse>> getTopUrls(
//            @RequestParam(defaultValue = "10") int limit) {
//        return ResponseEntity.ok(
//                analyticsService.getTopUrls(limit)
//        );
//    }

    @GetMapping("/{urlId}/browsers")
    public ResponseEntity<Map<String, Long>> getBrowserStats(
            @PathVariable Long urlId, Authentication authentication) {
        getUserOwnedUrl(urlId, authentication);
        return ResponseEntity.ok(
                analyticsService.getBrowserStats(urlId)
        );
    }

    @GetMapping("/{urlId}/devices")
    public ResponseEntity<Map<String, Long>> getDeviceStats(
            @PathVariable Long urlId, Authentication authentication) {
        getUserOwnedUrl(urlId, authentication);
        return ResponseEntity.ok(
                analyticsService.getDeviceStats(urlId)
        );
    }
    @GetMapping("/{urlId}/trends")
    public ResponseEntity<List<ClickTrendResponse>> getTrends(
            @PathVariable Long urlId, Authentication authentication) {
        getUserOwnedUrl(urlId, authentication);
        return ResponseEntity.ok(
                analyticsService.getClickTrends(urlId)
        );
    }

    private Url getUserOwnedUrl(Long urlId, Authentication authentication) {

        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new UrlNotFoundException("URL not found", HttpStatus.NOT_FOUND));

        if (!url.getUser().getEmail().equals(authentication.getName())) {
            throw new ForbiddenException("Access denied",HttpStatus.FORBIDDEN);
        }
        return url;
    }
}