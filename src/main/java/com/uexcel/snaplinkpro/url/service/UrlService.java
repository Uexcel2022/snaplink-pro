package com.uexcel.snaplinkpro.url.service;

import com.uexcel.snaplinkpro.auth.entity.User;
import com.uexcel.snaplinkpro.auth.repository.UserRepository;
import com.uexcel.snaplinkpro.url.dto.CreateUrlRequest;
import com.uexcel.snaplinkpro.url.dto.UrlResponse;
import com.uexcel.snaplinkpro.url.entity.Url;
import com.uexcel.snaplinkpro.url.repository.UrlRepository;
import com.uexcel.snaplinkpro.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final Base62Generator base62Generator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlResponse createUrl(
            CreateUrlRequest request,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String shortCode;

        if (request.getCustomAlias() != null &&
                !request.getCustomAlias().isBlank()) {

            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new RuntimeException("Alias already exists");
            }

            shortCode = request.getCustomAlias();

        } else {

            do {
                shortCode = base62Generator.generate();
            } while (urlRepository.existsByShortCode(shortCode));
        }

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .user(user)
                .build();

        urlRepository.save(url);

        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .clickCount(url.getClickCount())
                .build();
    }

    public List<UrlResponse> getMyUrls(Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return urlRepository.findByUserId(user.getId())
                .stream()
                .map(url -> UrlResponse.builder()
                        .id(url.getId())
                        .originalUrl(url.getOriginalUrl())
                        .shortCode(url.getShortCode())
                        .shortUrl(baseUrl + "/" + url.getShortCode())
                        .clickCount(url.getClickCount())
                        .build())
                .toList();
    }

    public void deleteUrl(Long id, Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        urlRepository.delete(url);
    }
}