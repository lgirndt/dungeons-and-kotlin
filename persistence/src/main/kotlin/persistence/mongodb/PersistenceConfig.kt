package io.dungeons.persistence.mongodb

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["io.dungeons.persistence.mongodb"])
class PersistenceConfig {
}