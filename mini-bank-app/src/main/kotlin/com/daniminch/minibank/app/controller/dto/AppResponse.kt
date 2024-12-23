package com.daniminch.minibank.app.controller.dto

import com.daniminch.minibank.app.service.error.ActionResult
import org.springframework.http.ResponseEntity

data class AppResponse<T>(
  val state: ActionStatus,
  val message: String,
  val data: T,
  val error: ErrorDescription? = null
) {

  class ErrorDescription(
    val code: String,
    val tags: Map<String, String>,
    val trace: String
  )

  companion object {

    fun <T> fromResult(
      actionResult: ActionResult<T>
    ): ResponseEntity<AppResponse<T>> {
      return ResponseEntity.ok(
        AppResponse(
          state = actionResult.state,
          message = actionResult.messageCode.name,
          data = actionResult.data
        )
      )
    }

    fun <T> ok(data: T): ResponseEntity<AppResponse<T>> {
      return ResponseEntity.ok(
        AppResponse(
          state = ActionStatus.OK,
          message = "",
          data = data
        )
      )
    }
  }
}
