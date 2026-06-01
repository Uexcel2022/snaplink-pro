package com.uexcel.snaplinkpro.analytics.service;

import com.uexcel.snaplinkpro.analytics.dto.AnalyticsResponse;
import com.uexcel.snaplinkpro.analytics.entity.Analytics;
import com.uexcel.snaplinkpro.analytics.event.UrlClickEvent;
import com.uexcel.snaplinkpro.analytics.repository.AnalyticsRepository;
import com.uexcel.snaplinkpro.url.entity.Url;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public void recordClick(UrlClickEvent event, Url url) {

        Analytics analytics = Analytics.builder()
                .url(url)
                .browser(extractBrowser(event.getUserAgent()))
                .device(extractDevice(event.getUserAgent()))
                .ipAddress(event.getIp())
                .referrer(event.getReferer())
                .build();

        analyticsRepository.save(analytics);
    }

    private String extractBrowser(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";

        return "Other";
    }

    private String extractDevice(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Mobile")) {
            return "Mobile";
        }

        return "Desktop";
    }

    public AnalyticsResponse getAnalytics(Url url) {

        List<Analytics> analytics = analyticsRepository.findByUrl(url);

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