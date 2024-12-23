package com.daniminch.minibank.app.controller

import com.daniminch.minibank.app.controller.dto.AppResponse
import com.daniminch.minibank.app.controller.dto.TransactionDto
import com.daniminch.minibank.app.controller.dto.UserCreateDto
import com.daniminch.minibank.app.model.Transaction
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ActiveProfiles("test")
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class OperationsTest(
  @LocalServerPort private var port: Int
) {

  val webClient: WebTestClient by lazy {
    check(port != 0)
    WebTestClient.bindToServer()
      .responseTimeout(Duration.ofMinutes(5))
      .baseUrl("http://localhost:$port").build()
  }

  private data class Client(
    val userId: Long,
    val login: String,
    val accountId: Long
  )

  private fun createClient(name: String): Client {
    var userId: Long? = null
    webClient.post().uri("/api/v1/user")
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        UserCreateDto(
          login = name
        )
      )
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
      .jsonPath("$.data.login").isEqualTo(name)
      .jsonPath("$.data.id").value<Int> { userId = it.toLong() }

    var accountId: Long? = null
    webClient.get().uri("/api/v1/account?userId=$userId")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.data[0].id").value<Int> { accountId = it.toLong() }

    return Client(userId!!, name, accountId!!)
  }

  private fun deposit(client: Client, amount: Double) {
    webClient.post().uri("/api/v1/account/${client.accountId}/deposit?amount=$amount")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
  }

  private fun withdraw(client: Client, amount: Double) {
    webClient.post().uri("/api/v1/account/${client.accountId}/withdraw?amount=$amount")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
  }

  private fun transfer(from: Client, to: Client, amount: Double) {
    webClient.post().uri("/api/v1/account/${from.accountId}/transfer?destinationLogin=${to.login}&amount=$amount")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
  }

  private fun transferFail(from: Client, to: Client, amount: Double) {
    webClient.post().uri("/api/v1/account/${from.accountId}/transfer?destinationLogin=${to.login}&amount=$amount")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("FAIL")
  }

  private fun getAvailable(client: Client): Double {
    var balance: Double = Double.MIN_VALUE
    webClient.get().uri("/api/v1/account/${client.accountId}")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("OK")
      .jsonPath("$.data.available").value<Double> { balance = it }

    return balance
  }

  val typeReference = object : ParameterizedTypeReference<AppResponse<List<TransactionDto>>>() {}

  private fun getTransactions(client: Client): List<TransactionDto> {
    return webClient.get().uri("/api/v1/account/${client.accountId}/transactions")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody(typeReference)
      .returnResult()
      .responseBody!!
      .data

  }

  private val logger = KotlinLogging.logger {}

  @Test
  fun testDepositAndWithdraw() {
    val client = createClient(UUID.randomUUID().toString())
    deposit(client, 100.0)
    assertEquals(100.0, getAvailable(client))
    deposit(client, 200.0)
    assertEquals(300.0, getAvailable(client))
    withdraw(client, 50.0)
    assertEquals(250.0, getAvailable(client))

    webClient.post().uri("/api/v1/account/${client.accountId}/withdraw?amount=500.0")
      .exchange().expectStatus().isEqualTo(HttpStatus.OK.value())
      .expectBody()
      .jsonPath("$.state").isEqualTo("FAIL")

    assertEquals(250.0, getAvailable(client))
    val trans = getTransactions(client)
    assertEquals(4, trans.size)
    assertEquals(1, trans.filter { it.state == Transaction.State.REJECTED }.size)
    assertEquals(3, trans.filter { it.state == Transaction.State.COMPLETED }.size)
  }

  @Test
  fun testTransfer() {
    val client1 = createClient(UUID.randomUUID().toString())
    val client2 = createClient(UUID.randomUUID().toString())
    deposit(client1, 100.0)
    var trans1 = getTransactions(client1)
    assertEquals(1, trans1.size)
    assertEquals(1, trans1.filter { it.state == Transaction.State.COMPLETED }.size)

    transfer(client1, client2, 70.0)
    assertEquals(30.0, getAvailable(client1))
    assertEquals(70.0, getAvailable(client2))

    trans1 = getTransactions(client1)
    assertEquals(2, trans1.size)
    assertEquals(2, trans1.filter { it.state == Transaction.State.COMPLETED }.size)

    var trans2 = getTransactions(client2)
    assertEquals(1, trans2.size)
    assertEquals(1, trans2.filter { it.state == Transaction.State.COMPLETED }.size)

    transferFail(client1, client2, 70.0)

    // not changed
    assertEquals(30.0, getAvailable(client1))
    assertEquals(70.0, getAvailable(client2))

    // added rejected
    trans1 = getTransactions(client1)
    assertEquals(3, trans1.size)
    assertEquals(1, trans1.filter { it.state == Transaction.State.REJECTED }.size)

    // haven't changed
    trans2 = getTransactions(client2)
    assertEquals(1, trans2.size)
    assertEquals(1, trans2.filter { it.state == Transaction.State.COMPLETED }.size)
  }
}