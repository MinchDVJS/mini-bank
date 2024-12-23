package com.daniminch.minibank.app.controller.dto

import com.daniminch.minibank.app.model.User

data class UserDto(
  val id: Long,
  val login: String,
  val status: User.Status
)