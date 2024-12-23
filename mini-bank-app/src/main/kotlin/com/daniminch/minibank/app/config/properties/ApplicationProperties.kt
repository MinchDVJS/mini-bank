package com.daniminch.minibank.app.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class ApplicationProperties(
  val persistDb: Boolean
)