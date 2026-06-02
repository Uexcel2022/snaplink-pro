package com.uexcel.snaplinkpro.service;

import com.uexcel.snaplinkpro.dashboard.dto.DashboardSummaryResponse;
import com.uexcel.snaplinkpro.dashboard.service.DashboardService;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardSummary() {

        String email = "test@example.com";

        when(urlRepository.countByUserEmail(email))
                .thenReturn(10L);

        when(urlRepository.countByUserEmailAndExpiresAtBefore(
                eq(email),
                any(LocalDateTime.class)
        )).thenReturn(2L);

        when(urlRepository.sumClicksByUserEmail(email))
                .thenReturn(150L);

        DashboardSummaryResponse response =
                dashboardService.getSummary(email);

        assertNotNull(response);

        assertEquals(10L,
                response.getTotalUrls());

        assertEquals(2L,
                response.getExpiredUrls());

        assertEquals(8L,
                response.getActiveUrls());

        assertEquals(150L,
                response.getTotalClicks());

        verify(urlRepository)
                .countByUserEmail(email);

        verify(urlRepository)
                .countByUserEmailAndExpiresAtBefore(
                        eq(email),
                        any(LocalDateTime.class)
                );

        verify(urlRepository)
                .sumClicksByUserEmail(email);
    }

    @Test
    void shouldReturnZeroActiveUrlsWhenAllUrlsExpired() {

        String email = "test@example.com";

        when(urlRepository.countByUserEmail(email))
                .thenReturn(5L);

        when(urlRepository.countByUserEmailAndExpiresAtBefore(
                eq(email),
                any(LocalDateTime.class)
        )).thenReturn(5L);

        when(urlRepository.sumClicksByUserEmail(email))
                .thenReturn(100L);

        DashboardSummaryResponse response =
                dashboardService.getSummary(email);

        assertEquals(5L,
                response.getTotalUrls());

        assertEquals(5L,
                response.getExpiredUrls());

        assertEquals(0L,
                response.getActiveUrls());

        assertEquals(100L,
                response.getTotalClicks());
    }
}
