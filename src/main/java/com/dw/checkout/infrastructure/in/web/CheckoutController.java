package com.dw.checkout.infrastructure.in.web;

import com.dw.checkout.application.port.in.CreatePreCheckoutUseCase;
import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.infrastructure.adapter.in.web.generated.api.OperationsApi;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutRequest;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.CheckoutSuccessResponse;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PreCheckoutRequest;
import com.dw.checkout.infrastructure.adapter.in.web.generated.model.PreCheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController implements OperationsApi {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CreatePreCheckoutUseCase createPreCheckoutUseCase;
    private final ProcessCheckoutUseCase processCheckoutUseCase;
    private final CheckoutWebMapper mapper;

    public CheckoutController(CreatePreCheckoutUseCase createPreCheckoutUseCase,
                              ProcessCheckoutUseCase processCheckoutUseCase,
                              CheckoutWebMapper mapper) {
        this.createPreCheckoutUseCase = createPreCheckoutUseCase;
        this.processCheckoutUseCase = processCheckoutUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PreCheckoutResponse> createPreCheckout(PreCheckoutRequest preCheckoutRequest) {
        int productCount = (preCheckoutRequest != null && preCheckoutRequest.getProducts() != null)
                ? preCheckoutRequest.getProducts().size()
                : 0;
        log.info("Iniciando pré-checkout | produtos={}", productCount);

        Cart cart = createPreCheckoutUseCase.execute(mapper.toProductSlugs(preCheckoutRequest));
        log.info("Pré-checkout concluído com sucesso | cartId={}", cart.getCartId());

        return ResponseEntity.ok(mapper.toPreCheckoutResponse(cart));
    }

    @Override
    public ResponseEntity<CheckoutSuccessResponse> processCheckout(CheckoutRequest checkoutRequest) {
        String paymentMethodName = (checkoutRequest != null && checkoutRequest.getPaymentMethod() != null)
                ? checkoutRequest.getPaymentMethod().getName()
                : "N/A";
        log.info("Processando checkout | cartId={} | metodo={}",
                checkoutRequest != null ? checkoutRequest.getCartId() : null,
                paymentMethodName);

        Cart cart = processCheckoutUseCase.execute(mapper.toCommand(checkoutRequest));
        log.info("Checkout concluído com sucesso | cartId={} | status={}", cart.getCartId(), cart.getStatus());

        return ResponseEntity.ok(mapper.toSuccessResponse(cart));
    }
}