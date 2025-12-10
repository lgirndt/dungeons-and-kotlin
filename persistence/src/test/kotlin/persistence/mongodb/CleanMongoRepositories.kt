package io.dungeons.persistence.mongodb

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.data.mongodb.repository.MongoRepository
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

/**
 * Annotation for MongoDB repository tests that automatically cleans up repositories before each test.
 *
 * Only cleans repositories that are injected as properties in the test class, making it faster
 * than cleaning all repositories in the Spring context.
 *
 * Usage:
 * ```
 * @DataMongoTest
 * @CleanMongoRepositories
 * class MyRepositoryTest {
 *     @Autowired
 *     private lateinit var repository: MongoDBSaveGameRepository
 *
 *     // No @BeforeEach cleanup needed - repository is cleaned automatically!
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MongoRepositoryCleanupExtension::class)
annotation class CleanMongoRepositories

/**
 * JUnit 5 extension that cleans up MongoRepository instances that are properties of the test class.
 */
class MongoRepositoryCleanupExtension : BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance

        // Find all properties in the test class that are MongoRepository instances
        testInstance::class.memberProperties
            .filter { property ->
                val javaType = property.returnType.javaType as? Class<*>
                javaType != null && MongoRepository::class.java.isAssignableFrom(javaType)
            }
            .forEach { property ->
                // Make private properties accessible
                property.isAccessible = true
                val repository = property.getter.call(testInstance) as? MongoRepository<*, *>
                repository?.deleteAll()
            }
    }
}
