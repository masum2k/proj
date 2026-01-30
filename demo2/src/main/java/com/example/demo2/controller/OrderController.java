package com.example.demo2.controller;

import com.example.demo2.entity.OrderEntity;
import com.example.demo2.repository.OrderRepository;
import com.example.grpc.common.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam String productId, @RequestParam int quantity) {

        ProductRequest productRequest = ProductRequest.newBuilder().setProductId(productId).build();
        ProductResponse productResponse = productStub.getProductById(productRequest);

        ReduceStockRequest stockRequest = ReduceStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        productStub.reduceStock(stockRequest);

        OrderEntity order = new OrderEntity();
        order.setProductId(productId);
        order.setProductName(productResponse.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(productResponse.getPrice() * quantity);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("orderId", order.getId());
        response.put("message", "Sipariş oluşturuldu.");
        response.put("totalPrice", order.getTotalPrice());

        return ResponseEntity.ok(response);
    }
}