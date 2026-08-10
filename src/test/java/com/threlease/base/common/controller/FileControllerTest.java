package com.threlease.base.common.controller;

import com.threlease.base.common.utils.storage.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileControllerTest {

    @Test
    void passesDownloadFlagThroughToService() {
        FileService fileService = mock(FileService.class);
        FileController controller = new FileController(fileService);
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/content/files/sample.txt");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/content/**");

        when(fileService.serve(eq("files/sample.txt"), eq("token-1"), eq(false)))
                .thenReturn(ResponseEntity.ok().build());

        controller.serve(request, "token-1", false);

        verify(fileService).serve("files/sample.txt", "token-1", false);
    }
}
