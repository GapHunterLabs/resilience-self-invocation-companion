package dev.gaphunter.resilienceselfinvocationcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaSelfInvocationFinderTest : BasePlatformTestCase() {

    fun `test unqualified self-invocation of a Retryable method is flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.java",
            """
            class PaymentService {
                void charge() {
                    callGateway();
                }

                @Retryable
                void callGateway() {}
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaSelfInvocationFinder.findAll(file).size)
    }

    fun `test this-qualified self-invocation of a CircuitBreaker method is flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.java",
            """
            class PaymentService {
                void charge() {
                    this.callGateway();
                }

                @CircuitBreaker(name = "gateway")
                void callGateway() {}
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaSelfInvocationFinder.findAll(file).size)
    }

    fun `test call through another object is not flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.java",
            """
            class PaymentService {
                void charge(GatewayClient gatewayClient) {
                    gatewayClient.callGateway();
                }
            }

            class GatewayClient {
                @Retryable
                void callGateway() {}
            }
            """.trimIndent(),
        )
        assertTrue(JavaSelfInvocationFinder.findAll(file).isEmpty())
    }

    fun `test a method with no resilience annotation is never flagged`() {
        val file = myFixture.configureByText(
            "PaymentService.java",
            """
            class PaymentService {
                void charge() {
                    callGateway();
                }

                void callGateway() {}
            }
            """.trimIndent(),
        )
        assertTrue(JavaSelfInvocationFinder.findAll(file).isEmpty())
    }
}
