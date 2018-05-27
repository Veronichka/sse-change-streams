package com.herasymenko.ssechangestream.inbound.database

import com.herasymenko.ssechangestream.domain.Train
import com.mongodb.client.model.changestream.ChangeStreamDocument
import org.bson.Document
import org.springframework.data.mongodb.core.messaging.MessageListener
import org.springframework.data.mongodb.core.messaging.Subscription

/**
 * Database changes listener port.
 */
interface ChangesPort {

    /**
     * Registers a new subscription request to the change messages listener container. Executes some actions on filtered messages according to the provided listener.
     * Call {@link #completeOnSubscription( final UUID subscriberId )} to unregister request when the work is done.
     * @param listener The listener that contains necessary actions on messages.
     * @param subscriberId the unique identifier of the subscriber.
     * @return The request subscription on messages.
     */
    Subscription subscribeToChangeStream( final MessageListener<ChangeStreamDocument<Document>, Train> listener, final UUID subscriberId )

    /**
     * Unregister the request to no more listen for the changes for the provided subscriber.
     * @param subscriberId the unique identifier of the subscriber.
     */
    void completeOnSubscription( final UUID subscriberId )
}
