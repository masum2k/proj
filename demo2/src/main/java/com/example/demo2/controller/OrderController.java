package com.example.demo2.controller;

import com.example.grpc.common.ProductRequest;
import com.example.grpc.common.ProductResponse;
import com.example.grpc.common.ProductServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    @GetMapping("/create/{productId}")
    public ResponseEntity<Map<String, Object>> createOrder(@PathVariable String productId) {

        // 1. Product Service'e gRPC çağrısı yap
        // Hata varsa burada PATLAR ve GlobalRestExceptionHandler yakalar.
        ProductRequest request = ProductRequest.newBuilder().setProductId(productId).build();
        ProductResponse productResponse = productStub.getProductById(request);

        // 2. Başarılıysa cevap dön
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sipariş oluşturuldu");
        response.put("productName", productResponse.getName());
        response.put("price", productResponse.getPrice());

        return ResponseEntity.ok(response);
    }
}