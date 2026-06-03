package com.uexcel.snaplinkpro.integration;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerIntegrationTest {

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private UrlCacheService urlCacheService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private ClickSyncScheduler clickSyncScheduler;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("urluser" + System.nanoTime() +"@test.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        token = objectMapper
                .readTree(responseBody)
                .get("token")
                .asText();
    }

    @Test
    void createUrl_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");

        mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    void createUrl_ShouldReturnCreated_WhenCustomAliasIsProvided() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com/custom");
        request.setCustomAlias("myalias");

        mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/custom"))
                .andExpect(jsonPath("$.shortCode").value("myalias"))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    void getMyUrls_ShouldReturnAuthenticatedUserUrls() throws Exception {

        CreateUrlRequest createRequest = new CreateUrlRequest();
        createRequest.setOriginalUrl("https://example.com");

        mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].originalUrl")
                        .value("https://example.com"));
    }

    @Test
    void deleteUrl_ShouldReturnNoContent_WhenUserOwnsUrl() throws Exception {

        CreateUrlRequest createRequest = new CreateUrlRequest();
        createRequest.setOriginalUrl("https://example.com/delete");

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long urlId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(delete("/api/urls/{id}", urlId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(redisTemplate).delete(anyString());
    }

    @Test
    void deleteUrl_ShouldReturnForbidden_WhenUserDoesNotOwnUrl() throws Exception {

        CreateUrlRequest createRequest = new CreateUrlRequest();
        createRequest.setOriginalUrl("https://example.com/private");

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long urlId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();

        String anotherUserToken = registerAndGetToken();

        mockMvc.perform(delete("/api/urls/{id}", urlId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + anotherUserToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void createUrl_ShouldReturnUnauthorized_WhenJwtIsMissing() throws Exception {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }



    private String registerAndGetToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Another User");
        request.setEmail("anotheruser@test.com");
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
