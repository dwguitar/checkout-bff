package com.dw.checkout.domain.exception;



public final class ErrorCatalog {

    private ErrorCatalog() {}

    public static final FrontendErrorSpec PRODUCT_NOT_FOUND = new FrontendErrorSpec(
            "BLOCK",
            "PRODUCT_NOT_FOUND",
            404,
            "P-1002",
            "Produto não encontrado",
            "O plano selecionado não está disponível no catálogo.",
            new FrontendErrorSpec.ActionButton("Voltar aos planos", "/planos"),
            null
    );

    public static final FrontendErrorSpec CART_NOT_FOUND = new FrontendErrorSpec(
            "BLOCK",
            "CART_NOT_FOUND",
            404,
            "P-1003",
            "Carrinho não encontrado",
            "Não localizamos sua sessão de checkout. Inicie novamente.",
            new FrontendErrorSpec.ActionButton("Reiniciar checkout", "/planos"),
            null
    );

    public static final FrontendErrorSpec INVALID_PAYMENT_METHOD = new FrontendErrorSpec(
            "INTERVENTION",
            "INVALID_PAYMENT_METHOD",
            400,
            "P-1004",
            "Meio de pagamento inválido",
            "Escolha uma das opções disponíveis para este carrinho ou verifique os dados informados.",
            new FrontendErrorSpec.ActionButton("Corrigir pagamento", "RETRY"),
            null
    );

    public static final FrontendErrorSpec CART_ALREADY_APPROVED = new FrontendErrorSpec(
            "BLOCK",
            "CART_ALREADY_APPROVED",
            409,
            "P-1005",
            "Pagamento já realizado",
            "Este carrinho já foi aprovado anteriormente e não pode ser cobrado novamente.",
            new FrontendErrorSpec.ActionButton("Ir para minha conta", "/conta"),
            null
    );

    public static final FrontendErrorSpec PAYMENT_DECLINED = new FrontendErrorSpec(
            "BLOCK",
            "PAYMENT_DECLINED",
            422,
            "P-1001",
            "Pagamento não aprovado",
            "Não foi possível concluir o pagamento. Tente novamente ou escolha outro meio.",
            new FrontendErrorSpec.ActionButton("Tentar novamente", "RETRY"),
            new FrontendErrorSpec.ActionButton("Trocar meio de pagamento", "/checkout/pagamento")
    );
}