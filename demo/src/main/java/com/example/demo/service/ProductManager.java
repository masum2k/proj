package com.example.demo.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductManager {

    private final RedisTemplate<String, String> redisTemplate;

    public String getProductName(String productId) {
        // Hata Senaryosu: ID 0 ise hata fırlat (Business Logic Exception)
        if ("0".equals(productId)) {
            throw new EntityNotFoundException("Ürün bulunamadı, ID geçersiz: " + productId);
        }

        // 1. Redis'e bak
        String cacheKey = "product:" + productId;
        String cachedName = redisTemplate.opsForValue().get(cacheKey);

        if (cachedName != null) {
            System.out.println("Redis'ten geldi: " + productId);
            return cachedName;
        }

        // 2. Redis'te yoksa "DB'den bulmuş gibi" yap ve Cache'e yaz
        String dbValue = "iPhone 15 Pro (DB)";
        redisTemplate.opsForValue().set(cacheKey, dbValue);
        System.out.println("DB'den geldi ve Redis'e yazıldı: " + productId);

        return dbValue;
    }
}