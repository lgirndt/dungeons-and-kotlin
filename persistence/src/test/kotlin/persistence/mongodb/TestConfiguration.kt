package io.dungeons.persistence.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.testcontainers.containers.MongoDBContainer

// DataMongoTest are looking for this annotation
@SpringBootConfiguration
// This only scans for Mongo Repositories, not other components
// We cannot instantiate repositories on our own, since they only exist as interfaces.
@EnableMongoRepositories(basePackages = ["io.dungeons.persistence.mongodb"])
class TestConfiguration {

    /**
     * Testcontainers MongoDB instance for integration tests.
     * The @ServiceConnection annotation automatically configures Spring's MongoDB connection.
     */
    @Bean
    @ServiceConnection
    fun mongoDBContainer(): MongoDBContainer {
        return MongoDBContainer("mongo:8.0")
    }

    /**
     * Configure MongoDB client settings with UUID representation.
     * This ensures UUIDs are stored and retrieved consistently in tests.
     */
    @Bean
    fun mongoClientSettings(container: MongoDBContainer): MongoClientSettings {
        return MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(container.connectionString))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build()
    }
}
