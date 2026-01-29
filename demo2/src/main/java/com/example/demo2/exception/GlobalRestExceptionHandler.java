package com.example.demo2.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Object> handleGrpcException(StatusRuntimeException e) {
        // gRPC Status kodunu al
        Status.Code grpcCode = e.getStatus().getCode();
        HttpStatus httpStatus;

        // gRPC -> HTTP Çevirimi
        switch (grpcCode) {
            case NOT_FOUND:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case INVALID_ARGUMENT:
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case UNAUTHENTICATED:
                httpStatus = HttpStatus.UNAUTHORIZED;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", httpStatus.value());
        body.put("error", grpcCode.name());
        body.put("message", e.getStatus().getDescription()); // gRPC'den gelen mesaj

        return new ResponseEntity<>(body, httpStatus);
    }
}