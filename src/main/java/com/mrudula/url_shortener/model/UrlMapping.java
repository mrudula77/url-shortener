package com.mrudula.url_shortener.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "url_mapping")
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(unique = true)
    private String shortCode;

    private LocalDateTime expiryDate;

    private int clickCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}
