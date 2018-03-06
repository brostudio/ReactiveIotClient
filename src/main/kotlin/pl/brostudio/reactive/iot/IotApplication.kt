package pl.brostudio.reactive.iot

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import pl.brostudio.reactive.iot.entities.TemperatureSensor
import reactor.core.publisher.toMono
import java.util.*

@SpringBootApplication
class IotApplication {

    @Bean
    fun webClient():WebClient {
        return WebClient.create("http://localhost:8080/iot")
    }

    @Bean
    fun commandLineRunner(webClient: WebClient) = CommandLineRunner {
        while(true) {
            webClient.get()
                    .uri("/all")
                    .retrieve()
                    .bodyToFlux(TemperatureSensor::class.java)
                    .flatMap { iot ->

                        val randomTemp = Random().nextFloat() * ((iot.temp + 2) - (iot.temp - 2))
                        val iotState = TemperatureSensor(iot.id, randomTemp)

                        webClient
                                .post()
                                .uri("/event")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromObject(iotState.toMono()))
                                .exchange()
                    }
                    .subscribe { println("${it}") }

            Thread.sleep(5000)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<IotApplication>(*args)
}
