package com.uexcel.snaplinkpro.analytics;


import com.uexcel.snaplinkpro.url.entity.Url;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;

    private String city;

    private String device;

    private String browser;

    @Column(columnDefinition = "TEXT")
    private String referrer;

    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url;

    @Builder.Default
    private LocalDateTime clickedAt = LocalDateTime.now();
}
