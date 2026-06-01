package com.uexcel.snaplinkpro.analytics.service;

import com.uexcel.snaplinkpro.analytics.dto.AnalyticsResponse;
import com.uexcel.snaplinkpro.analytics.dto.ClickTrendResponse;
import com.uexcel.snaplinkpro.analytics.dto.TopUrlResponse;
import com.uexcel.snaplinkpro.analytics.entity.Analytics;
import com.uexcel.snaplinkpro.analytics.event.UrlClickEvent;
import com.uexcel.snaplinkpro.analytics.repository.AnalyticsRepository;
import com.uexcel.snaplinkpro.url.entity.Url;

import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UrlRepository urlRepository;

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


    public List<TopUrlResponse> getTopUrls(int limit) {

        return urlRepository
                .findTopUrls(PageRequest.of(0, limit))
                .stream()
                .map(url -> TopUrlResponse.builder()
                        .shortCode(url.getShortCode())
                        .originalUrl(url.getOriginalUrl())
                        .clickCount(url.getClickCount())
                        .build())
                .toList();
    }

    public Map<String, Long> getBrowserStats(Long urlId) {

        return analyticsRepository.findByUrlId(urlId)
                .stream()
                .collect(Collectors.groupingBy(
                        Analytics::getBrowser,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getDeviceStats(Long urlId) {

        return analyticsRepository.findByUrlId(urlId)
                .stream()
                .collect(Collectors.groupingBy(
                        Analytics::getDevice,
                        Collectors.counting()
                ));
    }

    public List<ClickTrendResponse> getClickTrends(Long urlId) {

        List<LocalDateTime> timestamps =
                analyticsRepository.findAllCreatedAtByUrlId(urlId);

        Map<LocalDate, Long> grouped = timestamps.stream()
                .collect(Collectors.groupingBy(
                        LocalDateTime::toLocalDate,
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> ClickTrendResponse.builder()
                        .date(entry.getKey())
                        .clicks(entry.getValue())
                        .build())
                .toList();
    }



    private String extractBrowser(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Microsoft Edge")) return "Edge";
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