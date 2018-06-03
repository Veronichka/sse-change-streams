package com.herasymenko.ssechangestream.inbound.rest

import com.herasymenko.ssechangestream.core.SubscriptionPort
import com.herasymenko.ssechangestream.inbound.RestConstants
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Inbound HTTP gateway for subscribing to the train info streaming.
 */
@RestController
class Controller {

    /**
     * Knows how to work with subscribing.
     */
    private final SubscriptionPort subscriptionHandler

    Controller( final SubscriptionPort aSubscriptionHandler ) {
        subscriptionHandler = aSubscriptionHandler
    }

    @GetMapping( path = RestConstants.TRAIN_MAPPING, produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    ResponseEntity<SseEmitter> subscribe() {
        def emitter = subscriptionHandler.processSubscription()
        def headers =  new HttpHeaders()
        headers.add( HttpHeaders.CACHE_CONTROL, 'no-cache' )
        headers.add( 'X-Accel-Buffering', 'no' )
        new ResponseEntity<SseEmitter>( emitter, headers, HttpStatus.OK )
    }
}
