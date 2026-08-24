package com.acmecorp.payments;

import org.springframework.retry.annotation.Retryable;

/**
 * Demo data for Resilience Self-Invocation Companion — used with
 * `./gradlew runIde` to capture the real Marketplace screenshot. Open
 * this file, the warning icon should appear on the call inside
 * `charge`.
 */
public class PaymentService {

    public void charge() {
        // Self-invocation -- bypasses the AOP proxy. Never retried
        // on failure, silently. FLAGGED.
        callGateway();
    }

    @Retryable
    public void callGateway() {
        // ... calls an unreliable downstream payment gateway ...
    }
}
