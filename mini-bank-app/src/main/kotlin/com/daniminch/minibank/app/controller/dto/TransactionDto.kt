package com.daniminch.minibank.app.controller.dto

import com.daniminch.minibank.app.model.Transaction
import com.daniminch.minibank.app.model.Transaction.Action
import java.time.Instant
import java.util.UUID

data class TransactionDto(
  val id: UUID,
  val receiveTs: Instant,
  val info: String,
  val action: Action,
  val amount: Double,
  val state: Transaction.State
)
