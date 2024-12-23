package com.daniminch.minibank.app.service.error

enum class ErrorTag {
  ID,
  LOGIN,
  USER_ID,
  TYPE,
  CAUSE,
  AVAILABLE,
  TRANSACTION_ID;

  fun ofValue(value: Any?): Pair<ErrorTag, String> =
    this to value.toString()
}
