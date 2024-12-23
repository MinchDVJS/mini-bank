package com.daniminch.minibank.app.db

import com.daniminch.minibank.app.model.Transaction
import java.util.UUID

interface TransactionRepository {

  fun save(transaction: Transaction): Transaction
  fun get(transactionId: UUID): Transaction
  fun getByAccount(accountId: Long): List<Transaction>
}