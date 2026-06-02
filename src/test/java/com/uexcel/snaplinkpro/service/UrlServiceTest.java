package com.uexcel.snaplinkpro.service;

import com.uexcel.snaplinkpro.auth.entity.User;
import com.uexcel.snaplinkpro.auth.repository.UserRepository;
import com.uexcel.snaplinkpro.exception.AccessDeniedException;
import com.uexcel.snaplinkpro.exception.AliasAlreadyExistsException;
import com.uexcel.snaplinkpro.exception.ApiResponse;
import com.uexcel.snaplinkpro.url.dto.CreateUrlRequest;
import com.uexcel.snaplinkpro.url.dto.UrlResponse;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import com.uexcel.snaplinkpro.url.service.UrlCacheService;
import com.uexcel.snaplinkpro.url.service.UrlService;
import com.uexcel.snaplinkpro.util.Base62Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Base62Generator base62Generator;

    @Mock
    private UrlCacheService urlCacheService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UrlService urlService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                urlService,
                "baseUrl",
                "http://localhost:8080"
        );

        user = User.builder()
                .id(1L)
                .name("Udoka")
                .email("test@example.com")
                .password("password")
                .build();

//        when(authentication.getName())
//                .thenReturn("test@example.com");
    }

    @Test
    void shouldCreateUrlSuccessfullyWithGeneratedShortCode() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(base62Generator.generate())
                .thenReturn("abc123");

        when(urlRepository.save(any(Url.class)))
                .thenAnswer(invocation -> {
                    Url url = invocation.getArgument(0);
                    url.setId(1L);
                    return url;
                });
        when(authentication.getName())
                .thenReturn("test@example.com");

        UrlResponse response = urlService.createUrl(request, authentication);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("https://example.com", response.getOriginalUrl());
        assertEquals("abc123", response.getShortCode());
        assertEquals("http://localhost:8080/abc123", response.getShortUrl());
        assertEquals(0L, response.getClickCount());

        verify(base62Generator).generate();
        verify(urlRepository).save(any(Url.class));
        verify(urlCacheService).cacheUrl("abc123", "https://example.com");
    }

    @Test
    void shouldCreateUrlUsingCustomAlias() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");
        request.setCustomAlias("my-link");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(urlRepository.save(any(Url.class)))
                .thenAnswer(invocation -> {
                    Url url = invocation.getArgument(0);
                    url.setId(1L);
                    return url;
                });

        UrlResponse response =
                urlService.createUrl(request, authentication);

        assertEquals("my-link", response.getShortCode());

        verify(base62Generator, never()).generate();

        verify(urlCacheService)
                .cacheUrl("my-link", "https://example.com");
    }

    @Test
    void shouldThrowExceptionWhenAliasAlreadyExists() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");
        request.setCustomAlias("my-link");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(urlRepository)
                .save(any(Url.class));

        when(authentication.getName())
                .thenReturn("test@example.com");

        assertThrows(
                AliasAlreadyExistsException.class,
                () -> urlService.createUrl(request, authentication)
        );

        verify(urlCacheService, never())
                .cacheUrl(anyString(), anyString());
    }

    @Test
    void shouldDeleteUrlSuccessfully() {

        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .user(user)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(urlRepository.findById(1L))
                .thenReturn(Optional.of(url));

        urlService.deleteUrl(1L, authentication);

        verify(urlRepository).delete(url);

        verify(redisTemplate)
                .delete("url:abc123");
    }

    @Test
    void shouldThrowAccessDeniedWhenUserDoesNotOwnUrl() {

        User anotherUser = User.builder()
                .id(99L)
                .build();

        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .user(anotherUser)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(urlRepository.findById(1L))
                .thenReturn(Optional.of(url));

        assertThrows(
                AccessDeniedException.class,
                () -> urlService.deleteUrl(1L, authentication)
        );

        verify(urlRepository, never()).delete(any());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void shouldReturnPaginatedUserUrls() {

        Url url = Url.builder()
                .id(1L)
                .originalUrl("https://www.google.com")
                .shortCode("google")
                .clickCount(0L)
                .user(user)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Url> page = new PageImpl<>(
                List.of(url),
                pageable,
                1
        );

        when(urlRepository.findByUserEmail(
                "test@example.com",
                pageable))
                .thenReturn(page);

        ApiResponse<List<UrlResponse>> response =
                urlService.getUserUrls(
                        "test@example.com",
                        pageable
                );

        assertTrue(response.isSuccess());

        // data assertions
        assertEquals(1, response.getData().size());

        UrlResponse result = response.getData().get(0);
//
        assertEquals(1L, result.getId());
        assertEquals("https://www.google.com",
                result.getOriginalUrl());
        assertEquals("google",
                result.getShortCode());
        assertEquals("http://localhost:8080/google",
                result.getShortUrl());
        assertEquals(0L,
                result.getClickCount());

        // pagination assertions
        assertNotNull(response.getPagination());

        assertEquals(0,
                response.getPagination().getPage());

        assertEquals(10,
                response.getPagination().getSize());

        assertEquals(1,
                response.getPagination().getTotalElements());

        assertEquals(1,
                response.getPagination().getTotalPages());

        verify(urlRepository)
                .findByUserEmail(
                        "test@example.com",
                        pageable
                );
    }
}