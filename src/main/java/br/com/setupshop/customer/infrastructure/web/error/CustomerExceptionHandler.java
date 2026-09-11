package br.com.setupshop.customer.infrastructure.web.error;

import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.shared.infrastructure.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class CustomerExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerEmailAlreadyExistsException(
        EmailAlreadyExistsException exception, HttpServletRequest request) {

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
