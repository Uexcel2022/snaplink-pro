package com.uexcel.snaplinkpro.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UrlResponse {

    private Long id;

    private String originalUrl;

    private String shortCode;

    private String shortUrl;

    private Long clickCount;
}
