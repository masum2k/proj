package com.example.demo.exception;

import io.grpc.Status;
import io.grpc.StatusException;
import jakarta.persistence.EntityNotFoundException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(EntityNotFoundException.class)
    public StatusException handleNotFound(EntityNotFoundException e) {
        // Java Hatasını gRPC Status.NOT_FOUND'a çeviriyoruz
        return Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusException handleGeneral(Exception e) {
        return Status.INTERNAL
                .withDescription("Sunucu hatası: " + e.getMessage())
                .asException();
    }
}