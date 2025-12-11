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
 * Integration test for DateToKotlinInstantConverter.
 * Verifies that BSON DateTime is correctly deserialized to kotlin.time.Instant.
 */
@DataMongoTest
@CleanMongoRepositories
class DateToKotlinInstantConverterTest {

    @Autowired
    private lateinit var repository: TestEntityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun `should convert BSON DateTime to kotlin time Instant when reading from MongoDB`() {
        // Given: A BSON document with a DateTime field
        val testInstant = Instant.fromEpochMilliseconds(1704151445000) // 2024-01-02 03:04:05 UTC
        val document = Document().apply {
            put("_id", "test-id")
            put("timestamp", Date(testInstant.toEpochMilliseconds()))
        }
        mongoTemplate.getCollection(TEST_COLLECTION_NAME).insertOne(document)

        // When: Reading the entity via repository
        val entity = repository.findById("test-id")

        // Then: The BSON DateTime should be correctly deserialized to kotlin.time.Instant
        assertThat(entity).isPresent
        assertThat(entity.get().timestamp).isEqualTo(testInstant)
    }
}
