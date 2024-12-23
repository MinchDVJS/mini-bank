package com.daniminch.minibank.app.db

import com.daniminch.minibank.app.controller.dto.UserCreateDto
import com.daniminch.minibank.app.model.User

interface UserRepository {

  fun save(user: UserCreateDto): User
  fun get(userId: Long): User
  fun getByLogin(login: String): User
  fun updateStatus(id: Long, status: User.Status): User
}