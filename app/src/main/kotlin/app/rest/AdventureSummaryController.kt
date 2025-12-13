package io.dungeons.app.rest

import io.dungeons.port.AdventureSummaryResponse
import io.dungeons.port.ListAdventuresQuery
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AdventureSummaryController(private val listAdventuresQuery: ListAdventuresQuery) {

    @GetMapping("/adventures/summaries")
    fun findAll() : List<AdventureSummaryResponse> = listAdventuresQuery.query()

}