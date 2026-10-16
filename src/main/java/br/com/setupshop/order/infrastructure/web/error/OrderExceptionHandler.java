package br.com.setupshop.order.infrastructure.web.error;

import br.com.setupshop.order.domain.exception.InactiveCustomerException;
import br.com.setupshop.order.domain.exception.InactiveProductException;
import br.com.setupshop.shared.infrastructure.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(InactiveProductException.class)
    public ResponseEntity<ApiErrorResponse> handleInactiveProductException(
        InactiveProductException exception, HttpServletRequest request) {

        var status = HttpStatus.CONFLICT;

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(InactiveCustomerException.class)
    public ResponseEntity<ApiErrorResponse> handleInactiveCustomerException(
        InactiveCustomerException exception, HttpServletRequest request) {

        var status = HttpStatus.CONFLICT;

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
