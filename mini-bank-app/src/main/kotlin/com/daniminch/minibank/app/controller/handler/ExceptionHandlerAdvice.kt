package com.daniminch.minibank.app.controller.handler

import com.daniminch.minibank.app.controller.dto.ActionStatus
import com.daniminch.minibank.app.controller.dto.AppResponse
import com.daniminch.minibank.app.service.error.ApiError
import com.daniminch.minibank.app.service.error.ErrorCode
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandlerAdvice {

  private val logger = KotlinLogging.logger { }

  @ExceptionHandler(ApiError.Thrown::class)
  fun handleBusinessError(ex: ApiError.Thrown): ResponseEntity<AppResponse<Any?>> {
    logger.error(ex) { "Business Error Received" }

    val builder = when (ex.apiError.code) {
      ErrorCode.DUPLICATE_KEY, ErrorCode.NOT_FOUND -> ResponseEntity.badRequest()
      ErrorCode.INTERNAL_ERROR -> ResponseEntity.internalServerError()
      // Various business errors, in case we encounter them
      ErrorCode.COUNTERPARTY_ERROR,
      ErrorCode.INSUFFICIENT_BALANCE -> ResponseEntity.unprocessableEntity()
    }

    return builder.body(
      AppResponse(
        state = ActionStatus.FAIL,
        message = ex.apiError.code.toString(),
        data = null,
        error = AppResponse.ErrorDescription(
          code = ex.apiError.code.toString(),
          tags = ex.apiError.tags.mapKeys { it.key.toString() },
          trace = ex.apiError.trace.toString()
        )
      )
    )
  }
}