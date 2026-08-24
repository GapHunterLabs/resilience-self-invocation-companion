# Resilience Self-Invocation Companion

Warning icon on a call to a `@Retryable` (Spring Retry) or
`@CircuitBreaker`/`@Retry`/`@RateLimiter`/`@Bulkhead`/`@TimeLimiter`
(Resilience4j) method made from another method of the **same**
declaring class. All of these are AOP-proxy-based, the same mechanism
as Spring's own `@Transactional`/`@Cacheable`/`@Async`: self-invocation
bypasses the proxy entirely, so the resilience behavior is silently
never applied — no retry, no circuit breaker, no rate limit — with no
error and no warning.

Confirmed real: neither Spring Retry nor Resilience4j are covered by
JetBrains Ultimate's own Spring plugin (verified by inspecting its
bundled `plugin.xml`, which only registers this exact self-invocation
check for `@Transactional`/`@Cacheable`/`@Async`).

## Why it exists

`this.callGateway()` (or the unqualified `callGateway()`) compiles
fine and looks correct — the method really is annotated `@Retryable`.
It just never retries, because the call never goes through the proxy
that would apply the retry logic. The bug is invisible until a
downstream failure that should have retried simply doesn't.

## Why built this way

- **100% static PSI analysis** — matches each annotation by simple
  name, not by resolving the real classpath symbol, so it works
  whether the real Spring Retry/Resilience4j jars are on the classpath
  or not. Java and Kotlin.

## v0.1 scope — stated honestly, not exhaustively

Only flags an unqualified or `this.`-qualified call within the exact
same class — a call through an injected self-reference is correctly
never flagged, since it's qualified by a different expression.

## Usage

Open any Java/Kotlin class with a `@Retryable`/`@CircuitBreaker`/
`@Retry`/`@RateLimiter`/`@Bulkhead`/`@TimeLimiter` method. A call to
that method from elsewhere in the same class shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
