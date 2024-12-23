package com.daniminch.minibank.app.controller

import com.daniminch.minibank.app.controller.dto.AppResponse
import com.daniminch.minibank.app.controller.dto.TransactionDto
import com.daniminch.minibank.app.service.OperationsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/account/{accountId}")
class OperationsController(
  private val operationsService: OperationsService
) {

  @PostMapping("deposit")
  fun deposit(
    @PathVariable accountId: Long,
    @RequestParam amount: Double
  ): ResponseEntity<AppResponse<TransactionDto>> {
    val result = operationsService.deposit(accountId, amount)
    return AppResponse.fromResult(result)
  }

  @PostMapping("withdraw")
  fun withdraw(
    @PathVariable accountId: Long,
    @RequestParam amount: Double
  ): ResponseEntity<AppResponse<TransactionDto>> {
    val result = operationsService.withdraw(accountId, amount)
    return AppResponse.fromResult(result)
  }

  @PostMapping("transfer")
  fun transfer(
    @PathVariable accountId: Long,
    @RequestParam amount: Double,
    @RequestParam destinationLogin: String
  ): ResponseEntity<AppResponse<TransactionDto>> {
    val result = operationsService.transfer(accountId, amount, destinationLogin)
    return AppResponse.fromResult(result)
  }
}