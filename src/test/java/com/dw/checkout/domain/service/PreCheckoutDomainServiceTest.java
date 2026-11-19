package com.dw.checkout.domain.service;

import com.dw.checkout.application.port.out.CartRepositoryPort;
import com.dw.checkout.application.port.out.PaymentMethodPort;
import com.dw.checkout.application.port.out.ProductCatalogPort;
import com.dw.checkout.domain.exception.CheckoutBusinessException;
import com.dw.checkout.domain.exception.ErrorCatalog;
import com.dw.checkout.domain.model.Cart;
import com.dw.checkout.domain.model.CartStatus;
import com.dw.checkout.domain.model.PaymentMethod;
import com.dw.checkout.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreCheckoutDomainServiceTest {

    @Mock
    private ProductCatalogPort productCatalogPort;

    @Mock
    private PaymentMethodPort paymentMethodPort;

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    private PreCheckoutDomainService preCheckoutDomainService;

    @BeforeEach
    void setUp() {
        preCheckoutDomainService = new PreCheckoutDomainService(
                productCatalogPort,
                paymentMethodPort,
                cartRepositoryPort
        );
    }

    @Test
    @DisplayName("Deve criar carrinho de pré-checkout com sucesso, somar valores e carregar meios de pagamento")
    void shouldCreatePreCheckoutSuccessfully() {
        Product basico = new Product("basico", "Plano Básico", "Acesso individual em SD", new BigDecimal("19.90"), "BRL");
        Product premium = new Product("premium", "Plano Premium", "Acesso completo em HD", new BigDecimal("39.90"), "BRL");
        List<PaymentMethod> methods = List.of(
                new PaymentMethod("Cartão de Crédito"),
                new PaymentMethod("Cartão de Débito"),
                new PaymentMethod("Pix")
        );

        when(productCatalogPort.findBySlug("basico")).thenReturn(Optional.of(basico));
        when(productCatalogPort.findBySlug("premium")).thenReturn(Optional.of(premium));
        when(paymentMethodPort.findAvailableMethods()).thenReturn(methods);
        when(cartRepositoryPort.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = preCheckoutDomainService.execute(List.of("basico", "premium"));

        assertThat(result.getCartId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CartStatus.CREATED);
        assertThat(result.getProducts()).containsExactly(basico, premium);
        assertThat(result.getAvailablePaymentMethods()).hasSize(3);
        assertThat(result.totalAmount()).isEqualByComparingTo("59.80");

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepositoryPort).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getCartId()).isEqualTo(result.getCartId());
    }

    @Test
    @DisplayName("Deve lançar PRODUCT_NOT_FOUND quando algum slug não existir no catálogo")
    void shouldThrowProductNotFoundWhenSlugDoesNotExist() {
        Product premium = new Product("premium", "Plano Premium", "Acesso completo em HD", new BigDecimal("39.90"), "BRL");
        when(productCatalogPort.findBySlug("premium")).thenReturn(Optional.of(premium));
        when(productCatalogPort.findBySlug("plano-invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preCheckoutDomainService.execute(List.of("premium", "plano-invalido")))
                .isInstanceOf(CheckoutBusinessException.class)
                .satisfies(ex -> {
                    CheckoutBusinessException businessEx = (CheckoutBusinessException) ex;
                    assertThat(businessEx.getErrorSpec()).isEqualTo(ErrorCatalog.PRODUCT_NOT_FOUND);
                    assertThat(businessEx.getCartId()).isNull();
                });

        verify(cartRepositoryPort, never()).save(any());
        verify(paymentMethodPort, never()).findAvailableMethods();
    }

    @Test
    @DisplayName("Deve lançar PRODUCT_NOT_FOUND quando a lista de produtos for vazia ou nula")
    void shouldThrowProductNotFoundWhenProductListIsEmpty() {
        assertThatThrownBy(() -> preCheckoutDomainService.execute(List.of()))
                .isInstanceOf(CheckoutBusinessException.class)
                .satisfies(ex -> {
                    CheckoutBusinessException businessEx = (CheckoutBusinessException) ex;
                    assertThat(businessEx.getErrorSpec()).isEqualTo(ErrorCatalog.PRODUCT_NOT_FOUND);
                });

        verify(productCatalogPort, never()).findBySlug(any());
        verify(cartRepositoryPort, never()).save(any());
    }
}