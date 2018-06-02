package com.herasymenko.ssechangestream.core

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Works with client subscription.
 */
interface SubscriptionPort {

    /**
     * Create and process a sse connection.
     * @return fully assembled emitter.
     */
    SseEmitter processSubscription()
}