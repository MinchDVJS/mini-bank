package com.daniminch.minibank.app.controller.dto

data class AccountDto(
  val id: Long,
  val name: String,
  val available: Double,
)