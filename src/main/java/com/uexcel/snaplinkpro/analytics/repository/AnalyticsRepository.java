package com.uexcel.snaplinkpro.analytics.repository;

import com.uexcel.snaplinkpro.analytics.entity.Analytics;
import com.uexcel.snaplinkpro.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {

    List<Analytics> findByUrl(Url url);
}
