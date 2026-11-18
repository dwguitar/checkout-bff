package com.dw.checkout.infrastructure.in.web;

import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.exception.ErrorCatalog;
import com.dw.checkout.infrastructure.adapter.in.messasing.RabbitMQPaymentConsumer;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutErrorResponse;
import com.dw.checkout.infrastructure.observability.CheckoutMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final CheckoutWebMapper mapper;
    private final CheckoutMetrics checkoutMetrics;
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPaymentConsumer.class);

    public GlobalExceptionHandler(CheckoutWebMapper mapper, CheckoutMetrics checkoutMetrics) {
        this.mapper = mapper;
        this.checkoutMetrics = checkoutMetrics;
    }

    @ExceptionHandler(CheckoutBusinessException.class)
    public ResponseEntity<CheckoutErrorResponse> handleBusinessException(CheckoutBusinessException ex) {
        var spec = ex.getErrorSpec();
        var response = mapper.toErrorResponse(spec, ex.getCartId());
        checkoutMetrics.recordBusinessError(ex.getErrorSpec().errorCode());
        log.warn("Erro de negócio no checkout | cartId={} | errorCode={} | displayType={}");
        return ResponseEntity.status(spec.httpStatus()).body(response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CheckoutErrorResponse> handleValidationException(Exception ex) {
        var spec = ErrorCatalog.INVALID_PAYMENT_METHOD;
        var response = mapper.toErrorResponse(spec, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
