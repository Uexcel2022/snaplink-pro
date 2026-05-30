package com.uexcel.snaplinkpro.url.controller;

import com.uexcel.snaplinkpro.url.dto.CreateUrlRequest;
import com.uexcel.snaplinkpro.url.dto.UrlResponse;
import com.uexcel.snaplinkpro.url.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<UrlResponse> createUrl(
            @Valid @RequestBody CreateUrlRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.createUrl(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<UrlResponse>> getMyUrls(
            Authentication authentication) {

        return ResponseEntity.ok(
                urlService.getMyUrls(authentication)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            Authentication authentication) {

        urlService.deleteUrl(id, authentication);

        return ResponseEntity.noContent().build();
    }
}