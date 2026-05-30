package com.uexcel.snaplinkpro.url.entity;

import com.uexcel.snaplinkpro.auth.entity.User;
import com.uexcel.snaplinkpro.analytics.Analytics;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "urls")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(unique = true)
    private String customAlias;

    @Builder.Default
    private Long clickCount = 0L;

    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL)
    private List<Analytics> analytics;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
