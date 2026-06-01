package com.uexcel.snaplinkpro.analytics.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlClickEvent {
    private String shortCode;
    private String ip;
    private String userAgent;
    private String referer;
}
