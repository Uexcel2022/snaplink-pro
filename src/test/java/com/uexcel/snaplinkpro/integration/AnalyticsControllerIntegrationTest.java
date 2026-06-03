package com.uexcel.snaplinkpro.integration;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest {

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
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
    }

    @Test
    void getAnalytics_ShouldReturnEmptyAnalytics_WhenUrlHasNoClicks() throws Exception {

        String token = registerAndGetToken("Analytics User","analytics@test.com");

        JsonNode url = createUrl(token, "https://example.com/analytics");

        Long urlId = url.get("id").asLong();

        mockMvc.perform(get("/api/analytics/{urlId}", urlId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(0))
                .andExpect(jsonPath("$.desktopClicks").value(0))
                .andExpect(jsonPath("$.mobileClicks").value(0))
                .andExpect(jsonPath("$.chromeClicks").value(0))
                .andExpect(jsonPath("$.firefoxClicks").value(0))
                .andExpect(jsonPath("$.safariClicks").value(0));
    }


    @Test
    void getAnalytics_ShouldReturnUnauthorized_WhenJwtMissing() throws Exception {

        mockMvc.perform(get("/api/analytics/{urlId}", 1L))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void getAnalytics_ShouldReturnForbidden_WhenUserDoesNotOwnUrl() throws Exception {

        String ownerToken = registerAndGetToken(
                "Owner",
                "owner@test.com"
        );

        JsonNode url = createUrl(
                ownerToken,
                "https://example.com/private"
        );

        Long urlId = url.get("id").asLong();

        String anotherUserToken = registerAndGetToken(
                "Another User",
                "another@test.com"
        );

        mockMvc.perform(get("/api/analytics/{urlId}", urlId)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + anotherUserToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void getAnalytics_ShouldReturnNotFound_WhenUrlDoesNotExist() throws Exception {

        String token = registerAndGetToken(
                "User",
                "notfound@test.com"
        );

        mockMvc.perform(get("/api/analytics/{urlId}", 999999L)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTopUrls_ShouldReturnUserUrls() throws Exception {

        String token = registerAndGetToken(
                "Top User",
                "top@test.com"
        );

        createUrl(token, "https://example.com/one");
        createUrl(token, "https://example.com/two");

        mockMvc.perform(get("/api/analytics/top-urls")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    void getBrowserStats_ShouldReturnEmptyMap_WhenNoClicks() throws Exception {

        String token = registerAndGetToken(
                "Browser User",
                "browser@test.com"
        );

        JsonNode url = createUrl(
                token,
                "https://example.com/browser"
        );

        Long urlId = url.get("id").asLong();

        mockMvc.perform(get("/api/analytics/{urlId}/browsers", urlId)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }


    @Test
    void getDeviceStats_ShouldReturnEmptyMap_WhenNoClicks() throws Exception {

        String token = registerAndGetToken(
                "Device User",
                "device@test.com"
        );

        JsonNode url = createUrl(
                token,
                "https://example.com/device"
        );

        Long urlId = url.get("id").asLong();

        mockMvc.perform(get("/api/analytics/{urlId}/devices", urlId)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void getTrends_ShouldReturnEmptyList_WhenNoClicks() throws Exception {

        String token = registerAndGetToken(
                "Trend User",
                "trend@test.com"
        );

        JsonNode url = createUrl(
                token,
                "https://example.com/trend"
        );

        Long urlId = url.get("id").asLong();

        mockMvc.perform(get("/api/analytics/{urlId}/trends", urlId)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
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

    private String registerAndGetToken(String name, String email) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
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
