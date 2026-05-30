package com.uexcel.snaplinkpro.url.repository;

import com.uexcel.snaplinkpro.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
@RequestMapping
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    List<Url> findByUserId(Long userId);
}
