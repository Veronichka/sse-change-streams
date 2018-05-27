package com.herasymenko.ssechangestream.inbound.database

import com.herasymenko.ssechangestream.BaseIntegrationTest
import com.herasymenko.ssechangestream.domain.Train
import org.springframework.data.mongodb.core.messaging.Message
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import spock.lang.See
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

/**
 * Integration level test of {@link ChangesSubscriber}.
 */
@See( 'https://docs.mongodb.com/manual/changeStreams/' )
class ChangesSubscriberIntegrationTest extends BaseIntegrationTest {

    def 'exercise listening on database updates'() {
        given: 'valid subject to test'
        assert changesPort
        @Subject def subject = changesPort

        and: 'valid polling conditions'
        def conditions = new PollingConditions( timeout: 2000 )

        and: 'valid subscriber identifier'
        def subscriberId = UUID.randomUUID()

        and: 'valid message listener'
        def resultCache = []
        def listener = { Message it ->
            resultCache << it.body
            log.info( 'Messaging document: train is currently on station ' + it.body.currentStation )
        }

        and: 'valid train state exists in the system'
        def expTrainNumber = 'UNIQ_123'
        def trainState = new Train( trainNumber: expTrainNumber, currentStation: 'Lviv', dateCode: 100, startStation: 'Lviv', endStation: 'Kyiv' )
        mongoTemplate.insert( trainState )
        log.info( 'Inserting base train state document.' )

        and: 'expected updates on train location'
        def updateStates = ['Khmelnytskyi', 'Vinnytsia', 'Cherkasy'].collect { city ->
            new Train( trainNumber: expTrainNumber, currentStation: city, startStation: trainState.startStation, endStation: trainState.endStation )
        }

        when: 'the exercised subscriber is called'
        subject.subscribeToChangeStream( listener, subscriberId )

        and: 'the database is updated asynchronously'
        Thread.start {
            def criteria = Criteria.where( Train.FieldNames.TRAIN_NUMBER ).is( trainState.trainNumber )
            updateStates.each {
                sleep( 100 ) // without a small sleep it happens that the fullDocument is retrieved after the update operation but before the lookup
                // and cause the test to fail. It's known behavior from the mongo documentation
                def update = new Update().set( Train.FieldNames.CURRENT_STATION, it.currentStation )
                mongoTemplate.updateFirst( Query.query( criteria ), update, Train )
                log.info( 'Updating train location to ' + it.currentStation )
            }
        }

        then: 'waiting for the last database update'
        conditions.eventually {
            assert resultCache.size() == updateStates.size()
        }

        and: 'change messages are successfully processed by listener'
        resultCache.each { resultItem ->
            def expectedItem = updateStates.find { it.currentStation == resultItem.currentStation } // here currentStation is unique
            assert expectedItem
            with ( resultItem ) {
                trainNumber == expectedItem.trainNumber
                startStation == expectedItem.startStation
                endStation == expectedItem.endStation
            }
        }

        and: 'the subscription is registered'
        ChangesSubscriber.SUBSCRIPTIONS.size() == 1
        ChangesSubscriber.SUBSCRIPTIONS.containsKey( subscriberId )

        when: 'the method is called to unregister the subscriber from database changes'
        subject.completeOnSubscription( subscriberId )

        then: 'the subscriber is unregistered'
        !ChangesSubscriber.SUBSCRIPTIONS
    }
}
