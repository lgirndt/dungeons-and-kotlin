package io.dungeons.persistence.mongodb.converter

import io.dungeons.persistence.mongodb.CleanMongoRepositories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.*
import kotlin.time.Instant

/**
 * Integration test for KotlinInstantToDateConverter.
 * Verifies that kotlin.time.Instant is correctly serialized to BSON DateTime.
 */
@DataMongoTest
@CleanMongoRepositories
class KotlinInstantToDateConverterTest {
    @Autowired
    private lateinit var repository: TestEntityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun `should convert kotlin time Instant to BSON DateTime when writing to MongoDB`() {
        // Given: An entity with a kotlin.time.Instant
        val testInstant = Instant.fromEpochMilliseconds(1704151445000) // 2024-01-02 03:04:05 UTC
        val entity = TestEntity(
            id = "test-id",
            timestamp = testInstant,
        )

        // When: Saving the entity via repository
        repository.save(entity)

        // Then: The instant should be stored as BSON DateTime (java.util.Date), not as an object
        val rawDocument = mongoTemplate.getCollection(TEST_COLLECTION_NAME)
            .find(Document("_id", "test-id"))
            .first()

        assertThat(rawDocument).isNotNull
        val storedValue = rawDocument!!["timestamp"]

        // Verify it's stored as java.util.Date (BSON DateTime type)
        assertThat(storedValue)
            .withFailMessage(
                "Expected timestamp to be stored as java.util.Date (BSON DateTime), " +
                    "but was ${storedValue?.javaClass?.simpleName}: $storedValue",
            )
            .isInstanceOf(Date::class.java)

        // Verify the value is correct
        val storedDate = storedValue as Date
        assertThat(storedDate.time).isEqualTo(testInstant.toEpochMilliseconds())
    }
}
