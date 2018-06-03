package com.herasymenko.ssechangestream

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Base class to verify that the application can load properly.
 */
@CompileStatic // is necessary for webTestClient initializing
@SpringBootTest( webEnvironment = RANDOM_PORT )
@ContextConfiguration( classes = Application )
class RestIntegrationTest extends BaseIntegrationTest {

    @Value( '${local.server.port}' )
    protected int port

    @Autowired
    protected ApplicationContext applicationContext

    protected WebTestClient client

    void setup() {
        client = WebTestClient.bindToServer().baseUrl( "http://localhost:${port}" ).build()
    }

    protected UriComponentsBuilder baseURI() {
        UriComponentsBuilder.newInstance()
                .scheme( 'http' )
                .host( 'localhost' )
                .port( port )
                .path( '/' )
    }
}
