package com.herasymenko.ssechangestream.inbound.rest

import com.herasymenko.ssechangestream.RestIntegrationTest
import com.herasymenko.ssechangestream.domain.Train
import com.herasymenko.ssechangestream.inbound.RestConstants
import com.herasymenko.ssechangestream.inbound.domain.InitEventData
import com.herasymenko.ssechangestream.inbound.domain.TrainDetail
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.FluxExchangeResult
import reactor.test.StepVerifier

/**
 * Integration level test of the {@link Controller}.
 */
class ControllerIntegrationTest extends RestIntegrationTest {

    def 'exercise subscription endpoint'() {
        given: 'the valid train document is saved to the system'
        def expTrainNumber = 'UNIQ_123'
        def trainToInsert = new Train( trainNumber: expTrainNumber, currentStation: 'Lviv', dateCode: 100, startStation: 'Lviv', endStation: 'Kyiv' )
        mongoTemplate.insert( trainToInsert )
        log.info( 'Inserting document' )

        and: 'valid request uri'
        def validRequestUri = baseURI().path( RestConstants.TRAIN_MAPPING ).build().toUri()

        and: 'expected init event'
        def expInitEvent = new InitEventData(
                trainDetails: [new TrainDetail(
                        trainNumber: trainToInsert.trainNumber,
                        currentStation: trainToInsert.currentStation,
                        dateCode: trainToInsert.dateCode
                )]
        )

        and: 'expected updates to the system to listen for'
        def updates = ['Khmelnytskyi', 'Vinnytsia', 'Cherkasy'].collect { city ->
            new Train( trainNumber: trainToInsert.trainNumber,
                    currentStation: city,
                    dateCode: trainToInsert.dateCode,
                    startStation: trainToInsert.startStation,
                    endStation: trainToInsert.endStation )
        }

        and: 'expected update events'
        def expUpdateEvents = updates.collect { record ->
            new TrainDetail(
                    trainNumber: record.trainNumber,
                    currentStation: record.currentStation,
                    dateCode: record.dateCode
            )
        }

        when: 'the request is sent'
        FluxExchangeResult<String> result = client.get().uri( validRequestUri )
                .accept( MediaType.TEXT_EVENT_STREAM )
                .exchange()
                .returnResult( String ) // we have to return String because we get events of different types

        and: 'the database is updated asynchronously'
        Thread.start {
            sleep( 1500 ) // wait for listener to subscribe for changes from the database
            def criteria = Criteria.where( Train.FieldNames.TRAIN_NUMBER ).is( trainToInsert.trainNumber )
            updates.each {
                sleep( 300 ) // without a small sleep it happens that the fullDocument is retrieved after the update operation but before the lookup
                // and cause the test to fail. It's known behavior from the mongo documentation
                def update = new Update().set( Train.FieldNames.CURRENT_STATION, it.currentStation )
                mongoTemplate.updateFirst( Query.query( criteria ), update, Train )
                log.info( 'Updating train location to station ' + it.currentStation )
            }
        }

        then: 'the events are sent to the client as expected and consumed'
        def eventFlux = result.responseBody
        StepVerifier.create( eventFlux )
                .expectNext( objectMapper.writeValueAsString( expInitEvent ) )
                .expectNext( objectMapper.writeValueAsString( expUpdateEvents[0] ) )
                .expectNext( objectMapper.writeValueAsString( expUpdateEvents[1] ) )
                .expectNext( objectMapper.writeValueAsString( expUpdateEvents[2] ) )
                .verifyComplete()
    }
}
