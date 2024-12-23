package com.daniminch.minibank.app

import com.daniminch.minibank.app.config.properties.ApplicationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
class MiniBankTestAppApplication

fun main(args: Array<String>) {
  runApplication<MiniBankTestAppApplication>(*args)
}
