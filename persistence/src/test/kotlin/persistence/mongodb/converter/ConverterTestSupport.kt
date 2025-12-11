package io.dungeons.persistence.mongodb.converter

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import kotlin.time.Instant
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument

/**
 * Shared repository for converter integration tests.
 */
internal interface TestEntityRepository : MongoRepository<TestEntity, String>

/**
 * Shared test entity for converter integration tests.
 */
@MongoDocument
internal data class TestEntity(@Id val id: String, val timestamp: Instant)

/**
 * Collection name for test entities.
 */
internal const val TEST_COLLECTION_NAME = "testEntity"
