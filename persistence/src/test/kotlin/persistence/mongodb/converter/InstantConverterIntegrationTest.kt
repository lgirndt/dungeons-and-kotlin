package io.dungeons.persistence.mongodb.converter

import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.*
import kotlin.time.Instant
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument

/**
 * Integration test for kotlin.time.Instant serialization to BSON DateTime.
 */
@DataMongoTest
class InstantConverterIntegrationTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @AfterEach
    fun cleanup() {
        mongoTemplate.dropCollection(TEST_COLLECTION_NAME)
    }

    @Test
    fun `should serialize kotlin time Instant as BSON DateTime`() {
        // Given: A test entity with a kotlin.time.Instant
        val testInstant = Instant.fromEpochMilliseconds(1704151445000) // 2024-01-02 03:04:05 UTC
        val entity = TestInstantEntity(
            id = "test-id",
            createdAt = testInstant,
        )

        // When: Saving the entity
        mongoTemplate.save(entity, TEST_COLLECTION_NAME)

        // Then: The instant should be stored as BSON DateTime (java.util.Date), not as an object
        val rawDocument = mongoTemplate.getCollection(TEST_COLLECTION_NAME)
            .find(Document("_id", "test-id"))
            .first()

        assertThat(rawDocument).isNotNull
        val storedValue = rawDocument!!["createdAt"]

        // Verify it's stored as java.util.Date (BSON DateTime type)
        assertThat(storedValue)
            .withFailMessage(
                "Expected createdAt to be stored as java.util.Date (BSON DateTime), " +
                    "but was ${storedValue?.javaClass?.simpleName}: $storedValue"
            )
            .isInstanceOf(Date::class.java)

        // Verify the value is correct
        val storedDate = storedValue as Date
        assertThat(storedDate.time).isEqualTo(testInstant.toEpochMilliseconds())
    }

    @Test
    fun `should deserialize BSON DateTime to kotlin time Instant`() {
        // Given: A BSON document with a DateTime field
        val testInstant = Instant.fromEpochMilliseconds(1704151445000)
        val document = Document().apply {
            put("_id", "test-id")
            put("createdAt", Date(testInstant.toEpochMilliseconds()))
        }
        mongoTemplate.getCollection(TEST_COLLECTION_NAME).insertOne(document)

        // When: Reading the entity
        val entity = mongoTemplate.findById("test-id", TestInstantEntity::class.java, TEST_COLLECTION_NAME)

        // Then: The instant should be correctly deserialized
        assertThat(entity).isNotNull
        assertThat(entity!!.createdAt).isEqualTo(testInstant)
    }

    companion object {
        private const val TEST_COLLECTION_NAME = "test_instant_entities"
    }
}

/**
 * Test entity for verifying kotlin.time.Instant serialization.
 */
@MongoDocument
private data class TestInstantEntity(
    @Id val id: String,
    val createdAt: Instant,
)
