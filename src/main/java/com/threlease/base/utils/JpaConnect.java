package com.threlease.base.utils;

import com.threlease.base.utils.responses.BasicResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class JpaConnect {
    @FunctionalInterface
    public interface Callback {
        public ResponseEntity<Object> run(JpaConnect jpaConnect);
    }

    private final Callback callback;

    public JpaConnect(Callback callback) {
        this.callback = callback;
    }

    public ResponseEntity<Object> run() {
        try {
            if (this.callback != null) {
                return this.callback.run(this);
            } else {
                return createInternalServerErrorResponse("Internal server error.");
            }
        } catch (OptimisticLockException ex) {
            return createConflictResponse("Optimistic lock exception.");
        } catch (PersistenceException ex) {
            return createInternalServerErrorResponse("JPA operation error.");
        } catch (Exception ex) {
            return createInternalServerErrorResponse("Internal server error.");
        }
    }

    private ResponseEntity<Object> createInternalServerErrorResponse(String message) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private ResponseEntity<Object> createConflictResponse(String message) {
        return createErrorResponse(HttpStatus.CONFLICT, message);
    }

    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String message) {
        BasicResponse response = BasicResponse.builder()
                .success(false)
                .message(Optional.of(message))
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
