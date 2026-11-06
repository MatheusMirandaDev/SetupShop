package br.com.setupshop.account.infrastructure.web.error;

import br.com.setupshop.account.domain.exception.AccountNotFoundException;
import br.com.setupshop.order.domain.exception.InactiveProductException;
import br.com.setupshop.shared.infrastructure.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class AccountExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFoundException(
        AccountNotFoundException exception, HttpServletRequest request) {

        var status = HttpStatus.NOT_FOUND;

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
