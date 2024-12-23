package com.daniminch.minibank.app.controller

import com.daniminch.minibank.app.controller.dto.UserCreateDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@ActiveProfiles("test")
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class UserControllerTest(
  @LocalServerPort private var port: Int
) {

  val webClient: WebTestClient by lazy {
    check(port != 0)
    WebTestClient.bindToServer()
      .responseTimeout(Duration.ofSeconds(5))
      .baseUrl("http://localhost:$port").build()
  }

  @Test
  fun testAll() {
    var temp: Long? = null
    webClient.post().uri("/api/v1/user")
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        UserCreateDto(
          login = "test_login"
        )
      )
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
      .jsonPath("$.data.login").isEqualTo("test_login")
      .jsonPath("$.data.id").value<Int> { temp = it.toLong() }

    val id = temp!!

    webClient.get().uri("/api/v1/user/$id")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
      .jsonPath("$.data.id").isEqualTo(id)
      .jsonPath("$.data.login").isEqualTo("test_login")

    webClient.post().uri("/api/v1/user")
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        UserCreateDto(
          login = "test_login"
        )
      )
      .exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("FAIL")
      .jsonPath("$.error.code").isEqualTo("DUPLICATE_KEY")

    webClient.post().uri("/api/v1/user/$id/deactivate")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
      .jsonPath("$.data.id").isEqualTo(id)
      .jsonPath("$.data.login").isEqualTo("test_login")
      .jsonPath("$.data.status").isEqualTo("SUSPENDED")

    webClient.post().uri("/api/v1/user/$id/deactivate")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("WARN")
      .jsonPath("$.message").isEqualTo("USER_ALREADY_SUSPENDED")
      .jsonPath("$.data.id").isEqualTo(id)
      .jsonPath("$.data.login").isEqualTo("test_login")
      .jsonPath("$.data.status").isEqualTo("SUSPENDED")

  }
}