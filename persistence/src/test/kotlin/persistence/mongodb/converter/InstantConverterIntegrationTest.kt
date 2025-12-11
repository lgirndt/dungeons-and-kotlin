package io.dungeons.persistence.mongodb.converter

import io.dungeons.persistence.mongodb.CleanMongoRepositories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*
import kotlin.time.Instant
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument

/**
 * Integration test for kotlin.time.Instant serialization to BSON DateTime.
 */
@DataMongoTest
@CleanMongoRepositories
class InstantConverterIntegrationTest {

    @Autowired
    private lateinit var repository: TestInstantEntityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun `should serialize kotlin time Instant as BSON DateTime`() {
        // Given: A test entity with a kotlin.time.Instant
        val testInstant = Instant.fromEpochMilliseconds(1704151445000) // 2024-01-02 03:04:05 UTC
        val entity = TestInstantEntity(
            id = "test-id",
            createdAt = testInstant,
        )

        // When: Saving the entity via repository
        repository.save(entity)

        // Then: The instant should be stored as BSON DateTime (java.util.Date), not as an object
        val rawDocument = mongoTemplate.getCollection(COLLECTION_NAME)
            .find(Document("_id", "test-id"))
            .first()

        assertThat(rawDocument).isNotNull
        val storedValue = rawDocument!!["createdAt"]

        // Verify it's stored as java.util.Date (BSON DateTime type)
        assertThat(storedValue)
            .withFailMessage(
                "Expected createdAt to be stored as java.util.Date (BSON DateTime), " +
                    "but was ${storedValue?.javaClass?.simpleName}: $storedValue",
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
        mongoTemplate.getCollection(COLLECTION_NAME).insertOne(document)

        // When: Reading the entity via repository
        val entity = repository.findById("test-id")

        // Then: The instant should be correctly deserialized
        assertThat(entity).isPresent
        assertThat(entity.get().createdAt).isEqualTo(testInstant)
    }

    companion object {
        private const val COLLECTION_NAME = "testInstantEntity"
    }
}

/**
 * Repository for test entity.
 */
private interface TestInstantEntityRepository : MongoRepository<TestInstantEntity, String>

/**
 * Test entity for verifying kotlin.time.Instant serialization.
 */
@MongoDocument
private data class TestInstantEntity(
    @Id val id: String,
    val createdAt: Instant,
)
