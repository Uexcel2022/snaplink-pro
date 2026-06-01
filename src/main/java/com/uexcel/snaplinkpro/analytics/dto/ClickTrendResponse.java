package com.uexcel.snaplinkpro.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ClickTrendResponse {
    private LocalDate date;
    private Long clicks;
}