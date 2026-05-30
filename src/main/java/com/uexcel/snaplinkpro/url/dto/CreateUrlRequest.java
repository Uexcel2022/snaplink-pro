package com.uexcel.snaplinkpro.url.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    private String originalUrl;

    private String customAlias;
}
