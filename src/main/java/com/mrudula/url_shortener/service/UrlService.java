package com.mrudula.url_shortener.service;

import com.mrudula.url_shortener.model.UrlMapping;
import com.mrudula.url_shortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {
    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    // Base62 characters
    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Convert DB id → short code (Base62)
    private String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int)(id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    // Create short URL
    public String createShortUrl(String originalUrl, int expiryDays) {

        // Check if URL already shortened
        var existing = urlRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return baseUrl + "/" + existing.get().getShortCode();
        }

        // Save to DB first to get auto-generated ID
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setExpiryDate(LocalDateTime.now().plusDays(expiryDays));
        UrlMapping saved = urlRepository.save(mapping);

        // Generate short code from ID
        String shortCode = encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        // Cache in Redis for 1 day
        redisTemplate.opsForValue().set(
                shortCode, originalUrl, 1, TimeUnit.DAYS
        );

        return baseUrl + "/" + shortCode;
    }

    // Redirect: shortCode → original URL
    public String getOriginalUrl(String shortCode) {

        // Check Redis cache first (fast path ~4ms)
        String cached = redisTemplate.opsForValue().get(shortCode);
        if (cached != null) {
            updateClickCount(shortCode); // async update
            return cached;
        }

        // Cache miss → check DB (~40ms)
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // Check expiry
        if (mapping.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("URL has expired");
        }

        // Re-cache and update click count
        redisTemplate.opsForValue().set(
                shortCode, mapping.getOriginalUrl(), 1, TimeUnit.DAYS
        );
        updateClickCount(shortCode);

        return mapping.getOriginalUrl();
    }

    // Increment click count
    private void updateClickCount(String shortCode) {
        /*
        urlRepository.findByShortCode(shortCode).ifPresent(m -> {
            m.setClickCount(m.getClickCount() + 1);
            urlRepository.save(m);
        });*/
        System.out.println(">>> incrementing click for: " + shortCode);
        urlRepository.incrementClickCount(shortCode);
    }

    // Get analytics
    public UrlMapping getAnalytics(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }
}
