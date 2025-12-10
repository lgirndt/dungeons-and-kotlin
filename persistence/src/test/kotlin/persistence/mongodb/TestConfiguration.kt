package io.dungeons.persistence.mongodb

import org.springframework.boot.SpringBootConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

// DataMongoTest are looking for this annotation
@SpringBootConfiguration()
// This only scans for Mongo Repositories, now other components
// We cannot instantiate repositories on our own, since the only exist as interfaces.
@EnableMongoRepositories(basePackages = ["io.dungeons.persistence.mongodb"])
class TestConfiguration {

}
