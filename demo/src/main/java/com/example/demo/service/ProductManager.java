package com.example.demo.service;

import com.example.demo.exception.OutOfStockException;
import com.example.demo.model.ProductEntity;
import com.example.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            log.warn("Redis okunamadı: {}", e.getMessage());
        }

        //DB
        log.info("REDIS MISS: {}", productId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + productId));

        //Cache
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(10));

        return product;
    }

    @Transactional
    public ProductEntity reduceStock(String productId, int quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + productId));

        if (product.getStock() < quantity) {
            throw new OutOfStockException("Yetersiz stok! Mevcut: " + product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        ProductEntity updatedProduct = productRepository.save(product);

        // Cache'i temizle
        redisTemplate.delete("product:" + productId);

        log.info("Stok düşüldü. Ürün: {}, Adet: {}, Yeni Stok: {}", productId, quantity, updatedProduct.getStock());
        return updatedProduct;
    }

    // Admin için ürün ekleme
    public ProductEntity createProduct(ProductEntity product) {
        return productRepository.save(product);
    }
}