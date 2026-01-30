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
import java.util.List;

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

        //db
        log.info("REDIS MISS: {}", productId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + productId));

        //cache
        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(10));

        return product;
    }

    @Transactional
    public ProductEntity reduceStock(String productId, int quantity) {

        //db
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + productId));
        //business rule
        if (product.getStock() < quantity) {
            throw new OutOfStockException("Yetersiz stok! Mevcut: " + product.getStock());
        }
        //reduce stock
        product.setStock(product.getStock() - quantity);
        //db
        ProductEntity updatedProduct = productRepository.save(product);
        //redis cache
        redisTemplate.delete("product:" + productId);

        log.info("Stok düşüldü. Ürün: {}, Adet: {}, Yeni Stok: {}", productId, quantity, updatedProduct.getStock());
        return updatedProduct;
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }
}