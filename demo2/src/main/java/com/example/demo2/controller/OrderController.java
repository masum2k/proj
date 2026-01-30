package com.example.demo2.controller;

import com.example.demo2.dto.OrderResponse;
import com.example.demo2.dto.ProductDto;
import com.example.demo2.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getProductDetails(id));
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestParam String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(orderService.createOrder(productId, quantity));
    }

    @GetMapping(value = "/products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDto> streamProducts() {
        return orderService.getAvailableProductsStream();
    }
}