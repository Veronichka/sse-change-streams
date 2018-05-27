package com.herasymenko.ssechangestream.inbound.database

import com.herasymenko.ssechangestream.domain.Train
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import groovy.util.logging.Slf4j
import org.bson.Document
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest
import org.springframework.data.mongodb.core.messaging.MessageListener
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer
import org.springframework.data.mongodb.core.messaging.Subscription

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens for the change events {@see https://docs.mongodb.com/manual/changeStreams/} from the database.
 */
@Slf4j
class ChangesSubscriber implements ChangesPort {

    /**
     * The message listener container that is used to listen to the change events.
     */
    private final MessageListenerContainer container

    /**
     * Thread-safe collection of registered change stream requests under subscriber id.
     */
    final static Map<UUID, Subscription> SUBSCRIPTIONS = new ConcurrentHashMap<>()

    ChangesSubscriber(MessageListenerContainer aContainer ) {
        container = aContainer
    }

    @PostConstruct
    void init() {
        container.start()
        log.info( 'The database changes listener container is started.' )
    }

    @PreDestroy
    void destroy() {
        container.stop()
        log.info( 'The database changes listener container is stopped.' )
    }

    @Override
    Subscription subscribeToChangeStream( final MessageListener<ChangeStreamDocument<Document>, Train> listener, final UUID subscriberId ) {
        def request = ChangeStreamRequest.builder()
                .collection( 'train' ).publishTo( listener )
                .fullDocumentLookup( FullDocument.UPDATE_LOOKUP )
                .build()
        log.info( 'Register change stream request to the subscriber {}.', subscriberId )
        def subscription = container.register( request, Train )
        SUBSCRIPTIONS.put( subscriberId, subscription )
        subscription
    }

    @Override
    void completeOnSubscription( final UUID subscriberId ) {
        def subscription = SUBSCRIPTIONS.get( subscriberId )
        if ( subscription ) {
            container.remove( subscription )
            log.info( 'Unregister change stream request for the subscriber {}.', subscriberId )
            SUBSCRIPTIONS.remove( subscriberId )
        }
        else {
            log.info( 'Looks like current change stream request is already unregistered.' )
        }
    }
}
