package com.example.demo.grpc;

import com.example.demo.model.ProductEntity;
import com.example.demo.service.ProductManager;
import com.example.grpc.common.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductManager productManager;

    @Override
    public void getProductById(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        ProductEntity product = productManager.getProduct(request.getProductId());
        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setStock(product.getStock())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reduceStock(ReduceStockRequest request, StreamObserver<ReduceStockResponse> responseObserver) {
        ProductEntity product = productManager.reduceStock(request.getProductId(), request.getQuantity());
        ReduceStockResponse response = ReduceStockResponse.newBuilder()
                .setSuccess(true)
                .setRemainingStock(product.getStock())
                .setPrice(product.getPrice())
                .setName(product.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllProducts(Empty request, StreamObserver<ProductResponse> responseObserver){
        log.info("GetAllProducts çağrısı alındı, stream başlıyor...");

        List<ProductEntity> products = productManager.getAllProducts();

        for (ProductEntity product : products) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            ProductResponse response = ProductResponse.newBuilder()
                    .setProductId(product.getId())
                    .setName(product.getName())
                    .setPrice(product.getPrice())
                    .setStock(product.getStock())
                    .build();

            responseObserver.onNext(response);

            log.info("Ürün gönderildi: " + product.getName());
        }

        responseObserver.onCompleted();
        log.info("GetAllProducts stream tamamlandı.");
    }
}