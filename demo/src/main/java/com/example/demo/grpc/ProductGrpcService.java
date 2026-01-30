package com.example.demo.grpc;

import com.example.demo.model.ProductEntity;
import com.example.demo.service.ProductManager;
import com.example.grpc.common.ProductRequest;
import com.example.grpc.common.ProductResponse;
import com.example.grpc.common.ProductServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductManager productManager;

    @Override
    public void getProductById(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        ProductEntity product = productManager.getProduct(request.getProductId());

        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}