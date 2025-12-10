package io.dungeons.persistence.mongodb

import io.dungeons.domain.core.Id
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.util.*

@Configuration
class MongoConfiguration {
    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                IdWritingConverter(),
                IdReadingConverter()
            )
        )
    }
}

@WritingConverter
class IdWritingConverter : Converter<Id<*>, UUID> {
    override fun convert(source: Id<*>): UUID = source.value
}

@ReadingConverter
class IdReadingConverter : Converter<UUID, Id<*>> {
    override fun convert(source: UUID): Id<*> = Id.fromUUID<Any>(source)
}
