package com.uexcel.snaplinkpro.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyticsResponse {

    private Long totalClicks;

    private Long desktopClicks;

    private Long mobileClicks;

    private Long chromeClicks;

    private Long firefoxClicks;

    private Long safariClicks;
}