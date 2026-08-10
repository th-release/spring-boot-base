package com.threlease.base.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewControllerTest {

    @Test
    void forwardsNonApiRequestsToIndex() {
        ViewController controller = new ViewController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/dashboard");

        assertEquals("forward:/index.html", controller.forwardToIndex(request));
    }

    @Test
    void rejectsApiRequests() {
        ViewController controller = new ViewController();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        assertThrows(ResponseStatusException.class, () -> controller.forwardToIndex(request));
    }
}
