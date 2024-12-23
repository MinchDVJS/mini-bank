package com.daniminch.minibank.app.service.error

import com.daniminch.minibank.app.controller.dto.ActionStatus

enum class MessageCode {
  SUCCESS,
  TRANSACTION_FAILED,
  USER_ALREADY_SUSPENDED,
  ;

  fun <T> okOf(data: T): ActionResult<T> = ActionResult(
    state = ActionStatus.OK,
    messageCode = this,
    data = data
  )

  fun <T> warnOf(data: T): ActionResult<T> = ActionResult(
    state = ActionStatus.WARN,
    messageCode = this,
    data = data
  )

  fun <T> failOf(data: T, error: ApiError): ActionResult<T> = ActionResult(
    state = ActionStatus.FAIL,
    messageCode = this,
    data = data,
    error = error
  )
}