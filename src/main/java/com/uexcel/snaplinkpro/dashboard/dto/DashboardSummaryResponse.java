package com.uexcel.snaplinkpro.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private long totalUrls;
    private long totalClicks;
    private long activeUrls;
    private long expiredUrls;
}
