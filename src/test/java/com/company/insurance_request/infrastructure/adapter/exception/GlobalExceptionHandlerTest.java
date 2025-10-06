package com.company.insurance_request.infrastructure.adapter.exception;

import com.company.insurance_request.infrastructure.adapter.execption.GlobalError;
import com.company.insurance_request.infrastructure.adapter.execption.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Test
    void handleHttpMessageNotReadable_returnsBadRequestWithGlobalError() {
        handler = new GlobalExceptionHandler();
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("malformed", (Throwable) null);
        WebRequest request = mock(ServletWebRequest.class);
        when(((ServletWebRequest) request).getRequest()).thenReturn(mock(HttpServletRequest.class));
        when(((ServletWebRequest) request).getRequest().getRequestURI()).thenReturn("/api/test");

        var response = handler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(GlobalError.class);
        assertThat(((GlobalError) response.getBody()).getMessage()).isEqualTo("Payload invalid or malformed");
    }


    @Test
    void handleMissingServletRequestParameter_returnsBadRequestWithParameterName() {
        handler = new GlobalExceptionHandler();
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");
        WebRequest request = mock(ServletWebRequest.class);
        when(((ServletWebRequest) request).getRequest()).thenReturn(mock(HttpServletRequest.class));
        when(((ServletWebRequest) request).getRequest().getRequestURI()).thenReturn("/api/test");

        var response = handler.handleMissingServletRequestParameter(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(GlobalError.class);
        assertThat(((GlobalError) response.getBody()).getMessage()).contains("param");
    }


    @Test
    void handleNoHandlerFoundException_returnsNotFoundWithGlobalError() {
        handler = new GlobalExceptionHandler();
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/notfound", new HttpHeaders());
        WebRequest request = mock(ServletWebRequest.class);
        when(((ServletWebRequest) request).getRequest()).thenReturn(mock(HttpServletRequest.class));
        when(((ServletWebRequest) request).getRequest().getRequestURI()).thenReturn("/notfound");

        var response = handler.handleNoHandlerFoundException(ex, new HttpHeaders(), HttpStatus.NOT_FOUND, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(GlobalError.class);
        assertThat(((GlobalError) response.getBody()).getMessage()).isEqualTo("Resource not found");
    }


    @Test
    void handleAccessDenied_returnsForbiddenWithGlobalError() {
        handler = new GlobalExceptionHandler();
        AccessDeniedException ex = new AccessDeniedException("denied");
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/forbidden");

        var response = handler.handleAccessDenied(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(GlobalError.class);
        assertThat(response.getBody().getMessage()).isEqualTo("Acesso negado.");
    }


    @Test
    void handleGeneric_returnsInternalServerErrorWithGlobalError() {
        handler = new GlobalExceptionHandler();
        Exception ex = new Exception("error");
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/error");

        var response = handler.handleGeneric(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(GlobalError.class);
        assertThat(response.getBody().getMessage()).isEqualTo("Internal error");
    }


    @Test
    void path_returnsEmptyString_whenNotServletWebRequest() throws Exception {
        handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        var method = handler.getClass().getDeclaredMethod("path", WebRequest.class);
        method.setAccessible(true);
        var result = method.invoke(handler, request);
        assertThat(result).isEqualTo("");
    }


}
