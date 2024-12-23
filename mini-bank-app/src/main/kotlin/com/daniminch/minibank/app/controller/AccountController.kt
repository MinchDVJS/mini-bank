package com.daniminch.minibank.app.controller

import com.daniminch.minibank.app.controller.dto.AccountDto
import com.daniminch.minibank.app.controller.dto.AppResponse
import com.daniminch.minibank.app.controller.dto.TransactionDto
import com.daniminch.minibank.app.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
  private val accountService: AccountService
) {

  @GetMapping("{accountId}")
  fun getAccount(
    @PathVariable accountId: Long
  ): ResponseEntity<AppResponse<AccountDto>> {
    return AppResponse.ok(accountService.get(accountId))
  }

  // TODO Pagination ,etc
  @GetMapping
  fun listByUser(
    @RequestParam userId: Long
  ): ResponseEntity<AppResponse<List<AccountDto>>> {
    return AppResponse.ok(accountService.getByUserId(userId))
  }

  // TODO Pagination ,etc
  @GetMapping("{accountId}/transactions")
  fun getAccountTransactions(
    @PathVariable accountId: Long
  ): ResponseEntity<AppResponse<List<TransactionDto>>> {
    return AppResponse.ok(accountService.getAccountTransactions(accountId))
  }
}