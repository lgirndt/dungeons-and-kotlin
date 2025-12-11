package io.dungeons.persistence.mongodb.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.util.*
import kotlin.time.Instant

/**
 * Spring Data MongoDB converter for writing kotlin.time.Instant to BSON DateTime.
 * Converts Instant to java.util.Date which MongoDB stores as BSON DateTime.
 */
@WritingConverter
class KotlinInstantToDateConverter : Converter<Instant, Date> {
    override fun convert(source: Instant): Date = Date(source.toEpochMilliseconds())
}
