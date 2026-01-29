package com.example.demo.grpc;

import com.example.grpc.common.ProductRequest;
import com.example.grpc.common.ProductResponse;
import com.example.grpc.common.ProductServiceGrpc;
import com.example.demo.service.ProductManager;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductManager productManager;

    @Override
    public void getProductById(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        // Business Logic çağırılır (Try-Catch YOK! Handler yakalayacak)
        String productName = productManager.getProductName(request.getProductId());

        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(request.getProductId())
                .setName(productName)
                .setPrice(999.99)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}