package io.dungeons.persistence.mongodb.config

import io.dungeons.persistence.mongodb.converter.DateToKotlinInstantConverter
import io.dungeons.persistence.mongodb.converter.KotlinInstantToDateConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfig {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions = MongoCustomConversions(
        listOf(
            KotlinInstantToDateConverter(),
            DateToKotlinInstantConverter(),
        ),
    )
}
