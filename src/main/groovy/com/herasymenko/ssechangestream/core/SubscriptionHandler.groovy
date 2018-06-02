package com.herasymenko.ssechangestream.core

import com.herasymenko.ssechangestream.domain.Train
import com.herasymenko.ssechangestream.inbound.database.ChangesPort
import com.herasymenko.ssechangestream.inbound.domain.InitEventData
import com.herasymenko.ssechangestream.inbound.domain.TrainDetail
import com.herasymenko.ssechangestream.repository.TrainRepository
import groovy.util.logging.Slf4j
import org.bson.Document
import org.springframework.data.mongodb.core.messaging.Message
import org.springframework.http.MediaType
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * The handler of the client subscription to the database changes.
 */
@Slf4j
class SubscriptionHandler implements SubscriptionPort {

    /**
     * Number of milliseconds to hold the connection open, waiting for updates.
     */
    public static final long HOW_LONG_THE_CLIENT_WILL_WAIT_FOR_UPDATES = 60 * 1000

    /**
     * The name of update event.
     */
    public static final String UPDATE_EVENT_NAME = 'update'

    /**
     * The name of init event.
     */
    public static final String INIT_EVENT_NAME = 'init'

    /**
     * The task executor.
     */
    private final ThreadPoolTaskExecutor taskExecutor

    /**
     * Database changes listener.
     */
    private final ChangesPort changesPort

    /**
     * The repository for the train collection.
     */
    private final TrainRepository trainRepository

    SubscriptionHandler( final ThreadPoolTaskExecutor aTaskExecutor, final ChangesPort aChangesPort, final TrainRepository aTrainRepository ) {
        taskExecutor = aTaskExecutor
        changesPort = aChangesPort
        trainRepository = aTrainRepository
    }

    @Override
    SseEmitter processSubscription() {
        // every subscriber gets a dedicated emitter
        def subscriberID = UUID.randomUUID()

        def emitter = createEmitter( subscriberID )

        taskExecutor.execute {
            def listener = { Message<Document, Train> message ->
                log.info( 'The message with the train change from the database is received by the listener. The message body: {}.', message.body )
                sendUpdate( emitter, message.body, subscriberID )
            }

            def reportingJobs = trainRepository.findAll()
            sendInit( emitter, reportingJobs, subscriberID )
            changesPort.subscribeToChangeStream( listener, subscriberID )
        }
        emitter
    }

    /**
     * Send a init information of train currently running for subscriber.
     * @param emitter subscriber to notify.
     * @param trains the trains that are currently running.
     * @param subscriberId the unique subscriber identifier.
     */
    private void sendInit( final SseEmitter emitter, final List<Train> trains, final UUID subscriberId ) {
        def eventData = new InitEventData().with {
            trainDetails = trains.collect { item ->
                new TrainDetail().with {
                    trainNumber = item.trainNumber
                    currentStation = item.currentStation
                    dateCode = item.dateCode
                    it
                }
            }
            it
        }

        def builder = SseEmitter.event()
                .name( INIT_EVENT_NAME )
                .id( subscriberId as String ).reconnectTime ( 0 )
                .data( eventData, MediaType.APPLICATION_JSON )
        log.info( 'Sending {} event to the subscriber {} for train(s): {}.', INIT_EVENT_NAME, subscriberId, trains.trainNumber )
        emitter.send( builder )
    }

    /**
     * Notify the subscriber that a train state has changed.
     * @param emitter subscriber to notify.
     * @param state the train current state.
     * @param subscriberId the unique subscriber identifier.
     */
    private void sendUpdate( final SseEmitter emitter, final Train state, final UUID subscriberId ) {
        def eventData = new TrainDetail().with {
            trainNumber = state.trainNumber
            currentStation = state.currentStation
            dateCode = state.dateCode
            it
        }
        def builder = SseEmitter.event()
                .name( UPDATE_EVENT_NAME )
                .id( subscriberId as String ).reconnectTime( 0 )
                .data( eventData, MediaType.APPLICATION_JSON )
        log.info( 'Sending {} event to the subscriber {} for train(s): {}.', UPDATE_EVENT_NAME, subscriberId, state.trainNumber )
        emitter.send( builder )
    }

    /**
     * Creates a new emitter for the subscriber. Manages the callback scenarios.
     * @param subscriberId ID of the subscriber we are creating the emitter for.
     * @return fully assembled emitter.
     */
    private SseEmitter createEmitter( final UUID subscriberId ) {
        def emitter = new SseEmitter( HOW_LONG_THE_CLIENT_WILL_WAIT_FOR_UPDATES )
        installLifecycleCallbacks( subscriberId, emitter )
        emitter
    }

    /**
     * Adds in the necessary callbacks to clean up resources when connections get closed or timed out.
     * @param subscriber ID of the subscriber we are creating the emitter for.
     * @param emitter emitter to configure.
     */
    private void installLifecycleCallbacks( final UUID subscriberId, final SseEmitter emitter ) {
        def callback = {
            log.info( 'Cleaning up resources on completion for subscriber {}.', subscriberId )
            changesPort.completeOnSubscription( subscriberId )
        }
        def callBackTimeOut = {
            log.info( 'Cleaning up resources on timeout for subscriber {}.', subscriberId )
            changesPort.completeOnSubscription( subscriberId )
        }
        emitter.onCompletion( callback )
        emitter.onTimeout( callBackTimeOut )
    }
}
