package dev.gaphunter.resilienceselfinvocationcompanion.detect

/**
 * Matches Spring Retry's `@Retryable` and Resilience4j's
 * `@CircuitBreaker`/`@Retry`/`@RateLimiter`/`@Bulkhead`/`@TimeLimiter`
 * by simple annotation name, never by resolving the real classpath
 * symbol. All of these are AOP-proxy-based, same mechanism as Spring's
 * own `@Transactional`/`@Cacheable`/`@Async`: Baeldung's own Spring
 * Retry guide and Resilience4j's own documented behavior both confirm
 * "internal calls within the class bypass the proxy and, therefore,
 * are not retried" -- the annotation is silently ignored on
 * self-invocation, no error, no warning.
 */
object ResilienceSignals {
    private val ANNOTATION_NAMES = setOf(
        "Retryable", "CircuitBreaker", "Retry", "RateLimiter", "Bulkhead", "TimeLimiter",
    )

    fun isResilienceAnnotationName(simpleName: String): Boolean = simpleName in ANNOTATION_NAMES
}
