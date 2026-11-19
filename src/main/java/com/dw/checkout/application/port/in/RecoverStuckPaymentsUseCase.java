package com.dw.checkout.application.port.in;

import java.time.Instant;

public interface RecoverStuckPaymentsUseCase {
    void recoverBefore(Instant cutoff);
}
