package com.dw.checkout.domain.service;

import com.dw.checkout.application.port.in.ProcessCheckoutUseCase;
import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.PaymentEventPublisherPort;
import com.dw.checkout.application.port.out.PaymentGatewayPort;
import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.event.CheckoutPaymentEvent;
import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutDomainServiceTest {

    @Mock private ProductCatalogPort productCatalogPort;
    @Mock private PaymentMethodPort paymentMethodPort;
    @Mock private CartRepositoryPort cartRepositoryPort;
    @Mock private PaymentGatewayPort paymentGatewayPort;
    @Mock private PaymentEventPublisherPort paymentEventPublisherPort;

    private PreCheckoutDomainService preCheckoutService;
    private CheckoutDomainService checkoutService;

    @BeforeEach
    void setUp() {
        preCheckoutService = new PreCheckoutDomainService(productCatalogPort, paymentMethodPort, cartRepositoryPort);
        checkoutService = new CheckoutDomainService(cartRepositoryPort, paymentGatewayPort, paymentEventPublisherPort);
    }

    @Test
    @DisplayName("Deve criar pré-checkout com sucesso quando produto existe")
    void shouldCreatePreCheckoutSuccessfully() {
        var premium = new Product("premium", "Plano Premium", "Assinatura mensal", new BigDecimal("39.90"), "BRL");
        var methods = List.of(new PaymentMethod("Cartão de Crédito"), new PaymentMethod("Pix"));

        when(productCatalogPort.findBySlug("premium")).thenReturn(Optional.of(premium));
        when(paymentMethodPort.findAvailableMethods()).thenReturn(methods);
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart cart = preCheckoutService.execute(List.of("premium"));

        assertNotNull(cart.getCartId());
        assertEquals(CartStatus.CREATED, cart.getStatus());
        assertEquals(1, cart.getProducts().size());
        assertEquals(2, cart.getAvailablePaymentMethods().size());
    }

    @Test
    @DisplayName("Deve lançar PRODUCT_NOT_FOUND quando slug não existe")
    void shouldFailWhenProductDoesNotExist() {
        when(productCatalogPort.findBySlug("inexistente")).thenReturn(Optional.empty());

        var ex = assertThrows(CheckoutBusinessException.class,
                () -> preCheckoutService.execute(List.of("inexistente")));

        assertEquals("PRODUCT_NOT_FOUND", ex.getErrorSpec().errorCode());
        assertEquals("P-1002", ex.getErrorSpec().code());
    }

    @Test
    @DisplayName("Deve aprovar checkout com cartão, mascarar para os 4 últimos dígitos e publicar evento")
    void shouldApproveCardCheckoutAndMaskDigits() {
        Cart cart = createSampleCart();
        var card = new CardData("Ana Souza", "4111111111114242", "12/28", "123");

        when(cartRepositoryPort.findById(cart.getCartId())).thenReturn(Optional.of(cart));
        when(paymentGatewayPort.charge(any(), any(), any(), any())).thenReturn(new PaymentGatewayPort.PaymentResult(true, null));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart approved = checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(cart.getCartId(), "Cartão de Crédito", card));

        assertEquals(CartStatus.APROVADO, approved.getStatus());
        assertEquals("4242", approved.getReceipt().lastDigits());
        assertNull(approved.getReceipt().pixCode());
        verify(paymentEventPublisherPort).publish(any(CheckoutPaymentEvent.class));
    }

    @Test
    @DisplayName("Deve aprovar checkout com Pix gerando código Pix sem lastDigits e publicar evento")
    void shouldApprovePixCheckout() {
        Cart cart = createSampleCart();

        when(cartRepositoryPort.findById(cart.getCartId())).thenReturn(Optional.of(cart));
        when(paymentGatewayPort.charge(any(), any(), any(), any())).thenReturn(new PaymentGatewayPort.PaymentResult(true, "000201PIXCODE"));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart approved = checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(cart.getCartId(), "Pix", null));

        assertEquals(CartStatus.APROVADO, approved.getStatus());
        assertEquals("000201PIXCODE", approved.getReceipt().pixCode());
        assertNull(approved.getReceipt().lastDigits());
        verify(paymentEventPublisherPort).publish(any(CheckoutPaymentEvent.class));
    }

    @Test
    @DisplayName("Deve recusar pagamento quando gateway retornar falha (ex: cartão final 0002) e publicar evento de recusa")
    void shouldDeclineWhenGatewayRejects() {
        Cart cart = createSampleCart();
        var declinedCard = new CardData("Ana Souza", "4111111111110002", "12/28", "123");

        when(cartRepositoryPort.findById(cart.getCartId())).thenReturn(Optional.of(cart));
        when(paymentGatewayPort.charge(any(), any(), any(), any())).thenReturn(new PaymentGatewayPort.PaymentResult(false, null));
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        var ex = assertThrows(CheckoutBusinessException.class,
                () -> checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(cart.getCartId(), "Cartão de Crédito", declinedCard)));

        assertEquals("PAYMENT_DECLINED", ex.getErrorSpec().errorCode());
        assertEquals("P-1001", ex.getErrorSpec().code());
        assertEquals(CartStatus.DECLINED, cart.getStatus());
        verify(paymentEventPublisherPort).publish(any(CheckoutPaymentEvent.class));
    }

    @Test
    @DisplayName("Deve bloquear pagamento duplicado em carrinho já aprovado")
    void shouldBlockDuplicatePayment() {
        Cart cart = createSampleCart();
        cart.approve(new PaymentReceipt("Pix", null, "PIX", new BigDecimal("39.90"), "BRL", java.time.Instant.now()));

        when(cartRepositoryPort.findById(cart.getCartId())).thenReturn(Optional.of(cart));

        var ex = assertThrows(CheckoutBusinessException.class,
                () -> checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(cart.getCartId(), "Pix", null)));

        assertEquals("CART_ALREADY_APPROVED", ex.getErrorSpec().errorCode());
        verifyNoInteractions(paymentEventPublisherPort);
    }

    @Test
    @DisplayName("Deve falhar quando cartId não existir")
    void shouldFailWhenCartNotFound() {
        UUID randomId = UUID.randomUUID();
        when(cartRepositoryPort.findById(randomId)).thenReturn(Optional.empty());

        var ex = assertThrows(CheckoutBusinessException.class,
                () -> checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(randomId, "Pix", null)));

        assertEquals("CART_NOT_FOUND", ex.getErrorSpec().errorCode());
        verifyNoInteractions(paymentEventPublisherPort);
    }

    @Test
    @DisplayName("Deve falhar quando meio de pagamento não estiver disponível no carrinho")
    void shouldFailWhenPaymentMethodNotSupported() {
        Cart cart = createSampleCart();
        when(cartRepositoryPort.findById(cart.getCartId())).thenReturn(Optional.of(cart));

        var ex = assertThrows(CheckoutBusinessException.class,
                () -> checkoutService.execute(new ProcessCheckoutUseCase.CheckoutCommand(cart.getCartId(), "Boleto", null)));

        assertEquals("INVALID_PAYMENT_METHOD", ex.getErrorSpec().errorCode());
        verifyNoInteractions(paymentEventPublisherPort);
    }

    private Cart createSampleCart() {
        var product = new Product("premium", "Plano Premium", "Assinatura", new BigDecimal("39.90"), "BRL");
        var methods = List.of(new PaymentMethod("Cartão de Crédito"), new PaymentMethod("Pix"));
        return Cart.createNew(List.of(product), methods);
    }
}