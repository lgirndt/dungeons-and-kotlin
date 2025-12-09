package io.dungeons.persistence.mongodb.entities

import io.dungeons.domain.adventure.Adventure
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "adventure")
data class AdventureDocument(@Id val id: UUID, val name: String) {
    fun toDomain(): Adventure = Adventure(
        id = io.dungeons.domain.core.Id.fromUUID(id),
        name = name,
        initialRoomId = io.dungeons.domain.core.Id.generate(),
    )
}
