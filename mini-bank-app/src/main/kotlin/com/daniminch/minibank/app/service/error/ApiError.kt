package com.daniminch.minibank.app.service.error

import java.time.Instant
import java.util.UUID

class ApiError(
  val code: ErrorCode,
  val tags: Map<ErrorTag, String>,
  val trace: UUID = UUID.randomUUID(),
  val timestamp: Instant = Instant.now()
) {

  class Thrown(
    val apiError: ApiError
  ) : Throwable(apiError.code.name)

  fun thrown(): Thrown = Thrown(this)

  companion object {

    fun apiError(
      code: ErrorCode,
      vararg tags: Pair<ErrorTag, String>
    ): ApiError = ApiError(
      code = code,
      tags = mapOf(*tags)
    )

  }
}