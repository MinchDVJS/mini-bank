package com.daniminch.minibank.app.service.error

import com.daniminch.minibank.app.controller.dto.ActionStatus
import com.daniminch.minibank.app.controller.dto.AppResponse

class ActionResult<T>(
  val state: ActionStatus,
  val messageCode: MessageCode,
  val data: T,
  val error: ApiError? = null
) {

  fun toResponse() = AppResponse(
    state = state,
    message = messageCode.name,
    data = data,
  )

  companion object {

    fun <T> okResult(data: T): ActionResult<T> =
      MessageCode.SUCCESS.okOf(data)
  }
}