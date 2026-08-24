<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Resilience Self-Invocation Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning icon on a call to a `@Retryable`/`@CircuitBreaker`/`@Retry`/
  `@RateLimiter`/`@Bulkhead`/`@TimeLimiter` method made from another
  method of the same declaring class -- bypasses the AOP proxy
  silently.
- 100% static PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/resilience-self-invocation-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/resilience-self-invocation-companion/commits/0.1.0
