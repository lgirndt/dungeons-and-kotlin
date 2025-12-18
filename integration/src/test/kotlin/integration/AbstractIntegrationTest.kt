package io.dungeons.integration

import io.dungeons.api.rest.dto.AuthResponse
import io.dungeons.api.rest.dto.AuthenticationRequest
import io.dungeons.port.usecases.PlayerRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

private val logger = KotlinLogging.logger {}

/**
 * Base class for integration tests that bootstrap the full Spring Boot application context.
 *
 * Features:
 * - Starts the API application with a random port
 * - Provides MongoDB via testcontainers
 * - Auto-cleans database between tests
 * - Provides TestRestTemplate for HTTP calls
 * - Provides helper methods for authentication and common operations
 */
@SpringBootTest(
    classes = [IntegrationTestConfiguration::class],
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "spring.profiles.active=dev", // Required by ProfileValidator
        "spring.docker.compose.enabled=false", // Disable Docker Compose support, use Testcontainers
    ],
)
@AutoConfigureRestTestClient
abstract class AbstractIntegrationTest {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var restTestClient: RestTestClient

    @Autowired
    protected lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun cleanDatabase() {
        // Clean all collections before each test
        mongoTemplate.db.listCollectionNames().forEach { collectionName ->
            mongoTemplate.dropCollection(collectionName)
        }
        logger.debug { "Cleaned MongoDB collections before test" }
    }

    /**
     * Helper method to construct full URL for API endpoints
     */
    protected fun url(endpoint: String): String = "http://localhost:$port$endpoint"

    /**
     * Register a new player and return the access token
     */
    protected fun registerAndAuthenticate(
        playerName: String = "testplayer",
        password: String = "testpassword",
    ): String {
        // Register
        restTestClient
            .post()
            .uri(url("/auth/register"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(PlayerRequest(name = playerName, password = password))
            .exchange()
            .expectStatus()
            .isCreated

        // Login
        val loginResponse = restTestClient
            .post()
            .uri(url("/auth/login"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(AuthenticationRequest(username = playerName, password = password))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(AuthResponse::class.java)
            .returnResult()
            .responseBody

        return loginResponse?.accessToken
            ?: error("Failed to authenticate: no token in response")
    }

    /**
     * Make an authenticated GET request
     */
    protected fun authenticatedGet(endpoint: String, token: String): RestTestClient.ResponseSpec =
        restTestClient
            .get()
            .uri(url(endpoint))
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()

    /**
     * Make an authenticated POST request
     */
    protected fun <T : Any> authenticatedPost(endpoint: String, body: T, token: String): RestTestClient.ResponseSpec =
        restTestClient
            .post()
            .uri(url(endpoint))
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(body)
            .exchange()

    /**
     * Extension function to extract response body with type safety and null handling
     */
    protected fun <T : Any> RestTestClient.ResponseSpec.expectOkAndExtract(type: Class<T>): T {
        expectStatus().isOk
        return expectBody(type)
            .returnResult()
            .responseBody
            ?: error("No response body of type ${type.simpleName}")
    }
}
