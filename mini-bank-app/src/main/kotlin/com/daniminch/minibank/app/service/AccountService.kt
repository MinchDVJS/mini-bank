package com.daniminch.minibank.app.service

import com.daniminch.minibank.app.controller.dto.AccountDto
import com.daniminch.minibank.app.controller.dto.TransactionDto
import com.daniminch.minibank.app.db.AccountRepository
import com.daniminch.minibank.app.db.TransactionRepository
import com.daniminch.minibank.app.model.Account
import com.daniminch.minibank.app.model.Transaction
import com.daniminch.minibank.app.service.error.ActionResult
import com.daniminch.minibank.app.service.error.ApiError
import com.daniminch.minibank.app.service.error.MessageCode
import com.daniminch.minibank.app.service.error.counterPartyError
import com.daniminch.minibank.app.service.error.insufficientBalance
import com.daniminch.minibank.app.service.error.internalError
import com.daniminch.minibank.app.utils.KeyLockManager
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AccountService(
  private val accountRepository: AccountRepository,
  private val transactionRepository: TransactionRepository
) {

  private val logger = KotlinLogging.logger {}

  private val keyLockManager = KeyLockManager<Long>()

  fun getEntity(accountId: Long): Account {
    return accountRepository.get(accountId)
  }

  fun get(accountId: Long): AccountDto {
    return getEntity(accountId).toDto()
  }

  fun getByUserId(userId: Long): List<AccountDto> {
    return accountRepository.getByUserId(userId).map { it.toDto() }
  }

  fun <T> runLocked(accountId: Long, block: (Long) -> T): T {
    get(accountId)
    val res = keyLockManager.locked(accountId) {
      block(accountId)
    }
    return res
  }

  private fun failTransaction(ts: Transaction, error: ApiError): ActionResult<TransactionDto> {
    if (ts.state != Transaction.State.PENDING) {
      logger.warn { "Failing ${ts.state} ts ${ts.id} (${error.code})" }
    }
    ts.state = Transaction.State.REJECTED
    val res = transactionRepository.save(ts)
    return MessageCode.TRANSACTION_FAILED.failOf(res.toDto(), error)
  }

  fun applyTransaction(transaction: Transaction): ActionResult<TransactionDto> {
    if (!keyLockManager.hasLock(transaction.accountId)) {
      throw internalError("Modification without lock").thrown()
    }
    val account = accountRepository.get(transaction.accountId)
    if (transaction.action == Transaction.Action.WITHDRAW &&
      account.available < transaction.amount
    ) {
      val res = failTransaction(transaction, insufficientBalance(account.available))
      transaction.counterpartyTransactionId?.let {
        // TODO potential infinite loop
        failTransaction(transactionRepository.get(it), counterPartyError(transaction.id))
      }
      return res
    }

    val changeAmount = when (transaction.action) {
      Transaction.Action.DEPOSIT -> transaction.amount
      Transaction.Action.WITHDRAW -> -transaction.amount
    }

    transaction.state = if (transaction.completeWithCounterparty)
      Transaction.State.COMPLETED_AWAIT_COUNTER
    else
      Transaction.State.COMPLETED
    val res = transactionRepository.save(transaction)
    accountRepository.changeBalance(transaction.accountId, changeAmount)
    return ActionResult.okResult(res.toDto())
  }

  fun commitCounterTransactions(withdrawId: UUID, depositId: UUID): Pair<TransactionDto, TransactionDto> {
    val withdrawTs = transactionRepository.get(withdrawId)
    val depositTs = transactionRepository.get(depositId)
    withdrawTs.counterpartyTransactionId = depositTs.id
    depositTs.counterpartyTransactionId = withdrawTs.id
    if (
      withdrawTs.state == Transaction.State.COMPLETED_AWAIT_COUNTER
      && depositTs.state == Transaction.State.COMPLETED_AWAIT_COUNTER
    ) {
      withdrawTs.state = Transaction.State.COMPLETED
      depositTs.state = Transaction.State.COMPLETED
    } else {
      // This branch ATM unreachable, if we consider that all the actions running atomically
      withdrawTs.state = Transaction.State.REJECTED
      depositTs.state = Transaction.State.REJECTED
      // TBH will be much simpler to do this if we have detailed breakdwon of reserved money on account
      TODO("Rollback reserved money on accounts logic")
    }
    val newWithdraw = transactionRepository.save(withdrawTs)
    val newDeposit = transactionRepository.save(depositTs)
    return newWithdraw.toDto() to newDeposit.toDto()
  }

  fun getAccountTransactions(accountId: Long): List<TransactionDto> {
    return transactionRepository.getByAccount(accountId).map { it.toDto() }
  }
}

private fun Account.toDto() = AccountDto(
  id = id,
  name = name,
  available = available
)

private fun Transaction.toDto() = TransactionDto(
  id = id,
  receiveTs = receiveTs,
  info = info,
  action = action,
  amount = amount,
  state = state
)