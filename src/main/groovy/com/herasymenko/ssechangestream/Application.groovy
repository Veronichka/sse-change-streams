package com.herasymenko.ssechangestream

import com.herasymenko.ssechangestream.inbound.database.ChangesSubscriber
import com.herasymenko.ssechangestream.inbound.database.ChangesPort
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer

@SpringBootApplication
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
}
