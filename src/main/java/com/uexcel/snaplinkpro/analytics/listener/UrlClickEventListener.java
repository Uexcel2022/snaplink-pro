package com.uexcel.snaplinkpro.analytics.listener;

import com.uexcel.snaplinkpro.analytics.event.UrlClickEvent;
import com.uexcel.snaplinkpro.analytics.service.AnalyticsService;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

@Component
@RequiredArgsConstructor
public class UrlClickEventListener {

    private final UrlRepository urlRepository;
    private final AnalyticsService analyticsService;

    @Async
    @EventListener
    public void handle(UrlClickEvent event) {

        Url url = urlRepository.findByShortCode(event.getShortCode())
                .orElse(null);

        if (url == null) return;
        // store analytics
        analyticsService.recordClick(event, url);
    }
}