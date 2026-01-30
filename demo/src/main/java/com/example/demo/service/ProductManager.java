package com.example.demo.service;

import com.example.demo.model.ProductEntity;
import com.example.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductManager {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductEntity getProduct(String productId) {
        String cacheKey = "product:" + productId;

        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.info("REDIS HIT: {}", productId);
                return objectMapper.convertValue(cachedData, ProductEntity.class);
            }
        } catch (Exception e) {
            log.warn("Redis okunamadı, DB ile devam ediliyor: {}", e.getMessage());
        }

        log.info("REDIS MISS: {}", productId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + productId));

        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(10));

        return product;
    }
}