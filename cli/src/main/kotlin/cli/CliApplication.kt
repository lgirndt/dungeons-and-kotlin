package cli

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@SpringBootApplication
class CliApplication {

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder().build()
    }

    @Bean
    fun runner(restClient: RestClient) = CommandLineRunner {
        println("CLI Application started! What do you want to do?")

        print(">")
        val input = readln()
        println("You entered: $input")
        // Example: Make HTTP calls using restClient
        // val response = restClient.get()
        //     .uri("https://api.example.com/data")
        //     .retrieve()
        //     .body(String::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<CliApplication>(*args)
}
