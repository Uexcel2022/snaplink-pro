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
class DashboardControllerIntegrationTest {

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
    void getSummary_ShouldReturnZeroValues_WhenUserHasNoUrls() throws Exception {

        String token = registerAndGetToken(
                "Dashboard User",
                "dashboard@test.com"
        );

        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUrls").value(0))
                .andExpect(jsonPath("$.data.totalClicks").value(0))
                .andExpect(jsonPath("$.data.activeUrls").value(0))
                .andExpect(jsonPath("$.data.expiredUrls").value(0));
    }


    @Test
    void getSummary_ShouldReturnUnauthorized_WhenJwtMissing() throws Exception {

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSummary_ShouldReturnDashboardData() throws Exception {

        String token = registerAndGetToken(
                "Dashboard Owner",
                "owner-dashboard@test.com"
        );

        createUrl(token, "https://example.com/one");
        createUrl(token, "https://example.com/two");

        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUrls").value(2))
                .andExpect(jsonPath("$.data.activeUrls").value(2))
                .andExpect(jsonPath("$.data.expiredUrls").value(0))
                .andExpect(jsonPath("$.data.totalClicks").value(0));
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
}
