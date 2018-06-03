package com.herasymenko.ssechangestream

import com.fasterxml.jackson.databind.ObjectMapper
import com.herasymenko.ssechangestream.inbound.database.ChangesPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import spock.lang.Specification

@SpringBootTest
class BaseIntegrationTest extends Specification {

    protected final static Logger log = LoggerFactory.getLogger( BaseIntegrationTest )

    @Autowired
    protected MongoTemplate mongoTemplate

    @Autowired
    protected ChangesPort changesPort

    @Autowired
    protected ObjectMapper objectMapper

    void setup() {
        assert mongoTemplate
        resetMongoCollections()
    }

    /**
     * Remove all documents in non-system collections. Note that collections are NOT dropped.
     */
    protected void resetMongoCollections() {
        mongoTemplate.collectionNames.findAll { !it.startsWith( 'system.' ) }.each {
            mongoTemplate.remove( new Query(), it )
        }
    }
}
