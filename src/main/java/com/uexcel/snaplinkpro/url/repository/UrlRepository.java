package com.uexcel.snaplinkpro.url.repository;

import com.uexcel.snaplinkpro.url.entity.Url;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
@RequestMapping
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    List<Url> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + :clicks WHERE u.id = :id")
    void incrementClickCount(@Param("id") Long id, @Param("clicks") Long clicks);

    @Query("""
       SELECT u
       FROM Url u
       ORDER BY u.clickCount DESC
       """)
    List<Url> findTopUrls(Pageable pageable);
}
