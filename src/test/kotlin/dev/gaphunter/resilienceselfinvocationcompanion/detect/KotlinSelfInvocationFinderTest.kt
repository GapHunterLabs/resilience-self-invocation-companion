package dev.gaphunter.resilienceselfinvocationcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinSelfInvocationFinderTest : BasePlatformTestCase() {

    fun `test unqualified self-invocation of a RateLimiter method is flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.kt",
            """
            class PaymentService {
                fun charge() {
                    callGateway()
                }

                @RateLimiter(name = "gateway")
                fun callGateway() {}
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinSelfInvocationFinder.findAll(file).size)
    }

    fun `test this-qualified self-invocation of a Retry method is flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.kt",
            """
            class PaymentService {
                fun charge() {
                    this.callGateway()
                }

                @Retry(name = "gateway")
                fun callGateway() {}
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinSelfInvocationFinder.findAll(file).size)
    }

    fun `test call through another object is not flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.kt",
            """
            class PaymentService {
                fun charge(gatewayClient: GatewayClient) {
                    gatewayClient.callGateway()
                }
            }

            class GatewayClient {
                @Retryable
                fun callGateway() {}
            }
            """.trimIndent(),
        )
        assertTrue(KotlinSelfInvocationFinder.findAll(file).isEmpty())
    }

    fun `test a method with no resilience annotation is never flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.kt",
            """
            class PaymentService {
                fun charge() {
                    callGateway()
                }

                fun callGateway() {}
            }
            """.trimIndent(),
        )
        assertTrue(KotlinSelfInvocationFinder.findAll(file).isEmpty())
    }
}
