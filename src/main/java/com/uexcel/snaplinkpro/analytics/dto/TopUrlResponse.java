package com.uexcel.snaplinkpro.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopUrlResponse {
    private String shortCode;
    private String originalUrl;
    private Long clickCount;
}