package io.dungeons.persistence.mongodb.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.util.*
import kotlin.time.Instant

/**
 * Spring Data MongoDB converter for reading BSON DateTime to kotlin.time.Instant.
 * Converts java.util.Date (from MongoDB) to Instant.
 */
@ReadingConverter
class DateToKotlinInstantConverter : Converter<Date, Instant> {
    override fun convert(source: Date): Instant = Instant.fromEpochMilliseconds(source.time)
}
