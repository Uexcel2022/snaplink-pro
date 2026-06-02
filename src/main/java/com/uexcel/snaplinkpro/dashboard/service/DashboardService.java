package com.uexcel.snaplinkpro.dashboard.service;

import com.uexcel.snaplinkpro.dashboard.dto.DashboardSummaryResponse;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UrlRepository urlRepository;

    public DashboardSummaryResponse getSummary(String email) {

        long totalUrls = urlRepository.countByUserEmail(email);

        long expiredUrls = urlRepository
                .countByUserEmailAndExpiresAtBefore(
                        email,
                        LocalDateTime.now()
                );

        long totalClicks = urlRepository.sumClicksByUserEmail(email);

        long activeUrls = totalUrls - expiredUrls;

        return DashboardSummaryResponse.builder()
                .totalUrls(totalUrls)
                .totalClicks(totalClicks)
                .activeUrls(activeUrls)
                .expiredUrls(expiredUrls)
                .build();
    }
}