package com.dw.checkout.infrastructure.adapter.out.client;

import com.dw.checkout.application.port.out.PaymentGatewayPort;
import com.dw.checkout.domain.model.CardData;
import com.dw.checkout.domain.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGatewayAdapter.class);
    private static final String DECLINED_CARD_SUFFIX = "0002";

    @Override
    public PaymentResult charge(UUID cartId, PaymentMethod method, CardData cardData, BigDecimal amount) {
        String maskedCard = (cardData != null) ? "****" + cardData.lastDigits() : "N/A";
        log.info("Chamando gateway de pagamento | cartId={} | metodo={} | cartao={} | valor={}",
                cartId, method.name(), maskedCard, amount);

        if (method.isCard()) {
            return processCardPayment(cartId, cardData);
        }

        if (method.isPix()) {
            return processPixPayment(cartId, amount);
        }

        log.warn("Meio de pagamento não suportado no gateway | cartId={} | metodo={}", cartId, method.name());
        return new PaymentResult(false, null);
    }

    private PaymentResult processCardPayment(UUID cartId, CardData cardData) {
        boolean isDeclined = cardData != null
                && cardData.number() != null
                && cardData.number().endsWith(DECLINED_CARD_SUFFIX);

        if (isDeclined) {
            log.warn("Pagamento com cartão recusado pelo gateway | cartId={} | finalCartao={}",
                    cartId, DECLINED_CARD_SUFFIX);
            return new PaymentResult(false, null);
        }

        log.info("Pagamento com cartão aprovado pelo gateway | cartId={} | finalCartao={}",
                cartId, cardData != null ? cardData.lastDigits() : "N/A");
        return new PaymentResult(true, null);
    }

    private PaymentResult processPixPayment(UUID cartId, BigDecimal amount) {
        String simulatedPixCode = String.format(
                Locale.US,
                "00020126580014br.gov.bcb.pix0136%s5204000053039865405%.2f5802BR5913CKW Assinatura6009SAO PAULO6304ABCD",
                cartId,
                amount
        );

        log.info("Cobrança Pix gerada e aprovada pelo gateway | cartId={}", cartId);
        return new PaymentResult(true, simulatedPixCode);
    }
}