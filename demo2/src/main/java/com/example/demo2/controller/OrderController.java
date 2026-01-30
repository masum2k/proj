package com.example.demo2.controller;

import com.example.demo2.dto.OrderResponse;
import com.example.demo2.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestParam String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(orderService.createOrder(productId, quantity));
    }
}