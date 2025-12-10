package io.dungeons.persistence.mongodb

import io.dungeons.domain.core.Id
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.util.*

/**
 * MongoDB configuration for custom type conversions.
 * Registers converters for the Id<T> value class to/from UUID.
 */
@Configuration
class MongoDbConfiguration {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                IdToUuidConverter(),
                UuidToIdConverter()
            )
        )
    }
}

@WritingConverter
class IdToUuidConverter : Converter<Id<*>, UUID> {
    override fun convert(source: Id<*>): UUID = source.value
}

@ReadingConverter
class UuidToIdConverter : Converter<UUID, Id<*>> {
    override fun convert(source: UUID): Id<*> = Id.fromUUID<Any>(source)
}
