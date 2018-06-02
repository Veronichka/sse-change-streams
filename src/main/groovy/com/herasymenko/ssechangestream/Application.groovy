package com.herasymenko.ssechangestream

import com.herasymenko.ssechangestream.core.SubscriptionHandler
import com.herasymenko.ssechangestream.core.SubscriptionPort
import com.herasymenko.ssechangestream.inbound.database.ChangesSubscriber
import com.herasymenko.ssechangestream.inbound.database.ChangesPort
import com.herasymenko.ssechangestream.repository.TrainRepository
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@SpringBootApplication
@EnableMongoRepositories( 'com.herasymenko.ssechangestream.repository' )
class Application {

	static void main(String[] args) {
		SpringApplication.run Application, args
	}

    @Bean
    ChangesPort changesPort( MessageListenerContainer container ) {
        new ChangesSubscriber( container )
    }

    @Bean
    MessageListenerContainer container( MongoTemplate template ) {
        new DefaultMessageListenerContainer( template)
    }

    @Bean
    SubscriptionPort subscriptionPort( ThreadPoolTaskExecutor taskExecutor, ChangesPort changesPort, TrainRepository trainRepository ) {
        new SubscriptionHandler( taskExecutor, changesPort, trainRepository )
    }
}
