package com.simon.application.exception;

/**
 * A service this one depends on synchronously (see docs/MICROSERVICES.md's
 * sync-vs-async table) was unreachable or returned an unexpected error. No
 * retry/circuit-breaker is implemented - this just surfaces the failure clearly
 * instead of letting a raw low-level HTTP client exception leak to the caller.
 */
public class UpstreamServiceException extends RuntimeException {

    public UpstreamServiceException(String message) {
        super(message);
    }
}
