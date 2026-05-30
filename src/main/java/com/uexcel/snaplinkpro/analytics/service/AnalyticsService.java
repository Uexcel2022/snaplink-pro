package com.uexcel.snaplinkpro.analytics.service;

import com.uexcel.snaplinkpro.analytics.dto.AnalyticsResponse;
import com.uexcel.snaplinkpro.analytics.entity.Analytics;
import com.uexcel.snaplinkpro.analytics.repository.AnalyticsRepository;
import com.uexcel.snaplinkpro.url.entity.Url;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public void recordClick(
            Url url,
            HttpServletRequest request) {

        Analytics analytics = Analytics.builder()
                .url(url)
                .browser(extractBrowser(request))
                .device(extractDevice(request))
                .ipAddress(extractIpAddress(request))
                .referrer(request.getHeader("Referer"))
                .build();

        analyticsRepository.save(analytics);
    }

    private String extractBrowser(HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";

        return "Other";
    }

    private String extractDevice(HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Mobile")) {
            return "Mobile";
        }

        return "Desktop";
    }

    private String extractIpAddress(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }

        return request.getRemoteAddr();
    }


    public AnalyticsResponse getAnalytics(Url url) {

        var analytics = analyticsRepository.findByUrl(url);

        long desktop = analytics.stream()
                .filter(a -> "Desktop".equals(a.getDevice()))
                .count();

        long mobile = analytics.stream()
                .filter(a -> "Mobile".equals(a.getDevice()))
                .count();

        long chrome = analytics.stream()
                .filter(a -> "Chrome".equals(a.getBrowser()))
                .count();

        long firefox = analytics.stream()
                .filter(a -> "Firefox".equals(a.getBrowser()))
                .count();

        long safari = analytics.stream()
                .filter(a -> "Safari".equals(a.getBrowser()))
                .count();

        return AnalyticsResponse.builder()
                .totalClicks((long) analytics.size())
                .desktopClicks(desktop)
                .mobileClicks(mobile)
                .chromeClicks(chrome)
                .firefoxClicks(firefox)
                .safariClicks(safari)
                .build();
    }
}