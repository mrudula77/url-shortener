package com.mrudula.url_shortener.controller;

import com.mrudula.url_shortener.model.UrlMapping;
import com.mrudula.url_shortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
public class UrlController {
    @Autowired
    private UrlService urlService;

    // POST /shorten — create short URL
    @Operation(summary = "Shorten a URL")
    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shorten(
            @RequestBody Map<String, String> request) {

        String originalUrl = request.get("originalUrl");
        int expiryDays = Integer.parseInt(
                request.getOrDefault("expiryDays", "30")
        );

        String shortUrl = urlService.createShortUrl(originalUrl, expiryDays);
        return ResponseEntity.ok(Map.of("shortUrl", shortUrl));
    }

    // GET /{shortCode} — redirect to original
    @Operation(summary = "Redirect to original URL")
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    // GET /analytics/{shortCode} — view stats
    @Operation(summary = "Get URL analytics")
    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<UrlMapping> analytics(
            @PathVariable String shortCode) {

        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }
}
