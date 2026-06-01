package com.uexcel.snaplinkpro.scheduler;

import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import com.uexcel.snaplinkpro.url.service.UrlCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClickSyncScheduler {
    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;

    @Scheduled(fixedRate = 60000)
    public void syncClicksToDatabase() {

        boolean locked = urlCacheService.acquireLock("click-sync");

        if (!locked) return;

        try {

            List<Url> urls = urlRepository.findAll();

            for (Url url : urls) {

                Long clicks = urlCacheService.getClicks(url.getShortCode());

                if (clicks != null && clicks > 0) {

                    urlRepository.incrementClickCount(url.getId(), clicks);
                    urlCacheService.resetClicks(url.getShortCode());
                }
            }

        } finally {
            urlCacheService.releaseLock("click-sync");
        }
    }
}
