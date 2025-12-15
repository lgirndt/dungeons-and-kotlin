package io.dungeons.api.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private const val HELLO = "Hello!"

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello() = HELLO
}
