package com.uexcel.snaplinkpro.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.uexcel.snaplinkpro.analytics.service.AnalyticsService;
import com.uexcel.snaplinkpro.auth.dto.RegisterRequest;
import com.uexcel.snaplinkpro.ratelimit.RateLimitService;
import com.uexcel.snaplinkpro.scheduler.ClickSyncScheduler;
import com.uexcel.snaplinkpro.url.dto.CreateUrlRequest;
import com.uexcel.snaplinkpro.url.service.UrlCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;



import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedirectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private UrlCacheService urlCacheService;

    @MockitoBean
    private ClickSyncScheduler clickSyncScheduler;

    @MockitoBean
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
    }

    @Test
    void redirect_ShouldReturnFound_WhenShortCodeExistsInCache() throws Exception {

        when(urlCacheService.getCachedUrl("abc123"))
                .thenReturn("https://example.com");

        mockMvc.perform(get("/{shortCode}", "abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://example.com"
                ));
    }


    @Test
    void redirect_ShouldReturnFound_WhenCacheMissFallsBackToDatabase() throws Exception {

        String token = registerAndGetToken();

        JsonNode urlResponse = createUrl(
                token,
                "https://example.com/from-db"
        );

        String shortCode = urlResponse.get("shortCode").asText();

        when(urlCacheService.getCachedUrl(shortCode))
                .thenReturn(null);

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://example.com/from-db"
                ));
    }

    @Test
    void redirect_ShouldReturnNotFound_WhenShortCodeDoesNotExist() throws Exception {

        when(urlCacheService.getCachedUrl("invalid123"))
                .thenReturn(null);

        mockMvc.perform(get("/{shortCode}", "invalid123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_ShouldReturnTooManyRequests_WhenRateLimitExceeded() throws Exception {

        when(rateLimitService.isAllowed(anyString()))
                .thenReturn(false);

        mockMvc.perform(get("/{shortCode}", "abc123"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void redirect_ShouldIncrementClickCount() throws Exception {

        when(urlCacheService.getCachedUrl("abc123"))
                .thenReturn("https://example.com");

        mockMvc.perform(get("/{shortCode}", "abc123"))
                .andExpect(status().isFound());

        verify(urlCacheService).incrementClick("abc123");
    }




    private JsonNode createUrl(String token, String originalUrl) throws Exception {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl(originalUrl);

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

    }

    private String registerAndGetToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Redirect User");
        request.setEmail("redirectuser@test.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
