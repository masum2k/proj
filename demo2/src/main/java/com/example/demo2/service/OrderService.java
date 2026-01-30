package com.example.demo2.service;

import com.example.demo2.dto.OrderResponse;
import com.example.demo2.dto.ProductDto;
import com.example.demo2.entity.OrderEntity;
import com.example.demo2.repository.OrderRepository;
import com.example.grpc.common.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceStub productAsyncStub;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(String productId, int quantity) {
        ReduceStockRequest stockRequest = ReduceStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        ReduceStockResponse stockResponse = productStub.reduceStock(stockRequest);

        OrderEntity order = new OrderEntity();
        order.setProductId(productId);
        order.setProductName(stockResponse.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(stockResponse.getPrice() * quantity);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);

        return OrderResponse.builder()
                .status("SUCCESS")
                .orderId(order.getId())
                .message("Sipariş başarıyla oluşturuldu.")
                .totalPrice(order.getTotalPrice())
                .build();
    }

    public Flux<ProductDto> getAvailableProductsStream() {
        return Flux.create(sink -> productAsyncStub.getAllProducts(Empty.newBuilder().build(), new StreamObserver<ProductResponse>() {
            @Override
            public void onNext(ProductResponse response) {
                sink.next(new ProductDto(
                        response.getProductId(),
                        response.getName(),
                        response.getPrice(),
                        response.getStock()
                ));
            }

            @Override
            public void onError(Throwable t) {
                sink.error(t);
            }

            @Override
            public void onCompleted() {
                sink.complete();
            }
        }));
    }
}