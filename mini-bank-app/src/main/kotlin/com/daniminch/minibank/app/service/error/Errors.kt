package com.daniminch.minibank.app.service.error

import com.daniminch.minibank.app.service.error.ApiError.Companion.apiError
import java.util.UUID

fun duplicateLogin(value: String) = apiError(
  ErrorCode.DUPLICATE_KEY,
  ErrorTag.LOGIN.ofValue(value)
)

fun duplicateAccount(value: Long) = apiError(
  ErrorCode.DUPLICATE_KEY,
  ErrorTag.USER_ID.ofValue(value)
)

fun notFound(type: String, value: Any?) = apiError(
  ErrorCode.NOT_FOUND,
  ErrorTag.TYPE.ofValue(type),
  ErrorTag.ID.ofValue(value)
)

fun internalError(cause: String) = apiError(
  ErrorCode.INTERNAL_ERROR,
  ErrorTag.CAUSE.ofValue(cause),
)

fun insufficientBalance(available: Double) = apiError(
  ErrorCode.INSUFFICIENT_BALANCE,
  ErrorTag.AVAILABLE.ofValue(available),
)

fun counterPartyError(transactionId: UUID) = apiError(
  ErrorCode.COUNTERPARTY_ERROR,
  ErrorTag.TRANSACTION_ID.ofValue(transactionId),
)