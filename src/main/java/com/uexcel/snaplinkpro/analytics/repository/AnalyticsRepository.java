package com.uexcel.snaplinkpro.analytics.repository;

import com.uexcel.snaplinkpro.analytics.entity.Analytics;
import com.uexcel.snaplinkpro.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    List<Analytics> findByUrl(Url url);
    List<Analytics> findByUrlId(Long urlId);

    @Query("""
       SELECT a.clickedAt
       FROM Analytics a
       WHERE a.url.id = :urlId
       """)
    List<LocalDateTime> findAllCreatedAtByUrlId(@Param("urlId") Long urlId);
}
