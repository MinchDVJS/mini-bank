package com.daniminch.minibank.app.service

import com.daniminch.minibank.app.controller.dto.ActionStatus
import com.daniminch.minibank.app.controller.dto.TransactionDto
import com.daniminch.minibank.app.model.Transaction
import com.daniminch.minibank.app.service.error.ActionResult
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class OperationsService(
  private val userService: UserService,
  private val accountService: AccountService
) {

  fun deposit(
    accountId: Long,
    amount: Double
  ): ActionResult<TransactionDto> {

    val res = accountService.runLocked(accountId) {
      val ts = Transaction(
        // TODO something more reliable
        id = UUID.randomUUID(),
        accountId = accountId,
        receiveTs = Instant.now(),
        action = Transaction.Action.DEPOSIT,
        info = "Manual deposit",
        amount = amount
      )
      accountService.applyTransaction(ts)
    }
    return res
  }

  fun withdraw(
    accountId: Long,
    amount: Double
  ): ActionResult<TransactionDto> {

    val res = accountService.runLocked(accountId) {
      val ts = Transaction(
        // TODO something more reliable
        id = UUID.randomUUID(),
        accountId = accountId,
        receiveTs = Instant.now(),
        action = Transaction.Action.WITHDRAW,
        info = "Manual withdraw",
        amount = amount
      )
      accountService.applyTransaction(ts)
    }
    return res
  }

  fun transfer(
    accountId: Long,
    amount: Double,
    destinationLogin: String
  ): ActionResult<TransactionDto> {
    val fromAcc = accountService.getEntity(accountId)
    val fromUser = userService.get(fromAcc.userId)
    val destinationUser = userService.getByLogin(destinationLogin)
    val destinationAcc = accountService.getByUserId(destinationUser.id).first()

    // TODO something more reliable with IDs (timestamp)
    val withdrawTsId = UUID.randomUUID()

    val resWithdraw = accountService.runLocked(accountId) {
      val ts = Transaction(
        id = withdrawTsId,
        accountId = accountId,
        receiveTs = Instant.now(),
        action = Transaction.Action.WITHDRAW,
        info = "Transfer to $destinationLogin",
        completeWithCounterparty = true,
        amount = amount
      )
      accountService.applyTransaction(ts)
    }

    if (resWithdraw.state != ActionStatus.OK) return resWithdraw

    val depositTsId = UUID.randomUUID()
    accountService.runLocked(destinationAcc.id) {
      val ts = Transaction(
        id = depositTsId,
        accountId = destinationAcc.id,
        receiveTs = Instant.now(),
        action = Transaction.Action.DEPOSIT,
        info = "Transfer from ${fromUser.login}",
        completeWithCounterparty = true,
        amount = amount
      )
      accountService.applyTransaction(ts)
    }

    val (finalWithdraw, _) = accountService.commitCounterTransactions(
      withdrawTsId, depositTsId
    )


    return ActionResult.okResult(finalWithdraw)
  }

}