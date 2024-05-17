package com.threlease.base.utils.security;

import com.threlease.base.utils.responses.BasicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {NullPointerException.class})
    public ResponseEntity<?> handleNullPointerException(NullPointerException ex, WebRequest request) {
        BasicResponse response = BasicResponse.builder()
                .success(false)
                .message(Optional.of("잘못된 요청입니다."))
                .data(Optional.empty())
                .build();

        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> messagesRow = new ArrayList<>();
        for (ObjectError error: ex.getBindingResult().getAllErrors()) {
            boolean status = true;

            for (String message: messagesRow) {
                if (message.equals(error.getDefaultMessage()))
                    status = false;
            }

            if (status)
                messagesRow.add(error.getDefaultMessage());
        }

        StringBuilder messages = new StringBuilder();
        for (String error: messagesRow) {
            messages.append(error).append("\n");
        }

        return ResponseEntity.status(400).body(
                BasicResponse.builder()
                        .success(false)
                        .message(Optional.of(messages.toString()))
                        .build()
        );
    }
}
