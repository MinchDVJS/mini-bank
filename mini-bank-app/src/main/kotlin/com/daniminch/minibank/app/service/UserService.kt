package com.daniminch.minibank.app.service

import com.daniminch.minibank.app.controller.dto.UserCreateDto
import com.daniminch.minibank.app.controller.dto.UserDto
import com.daniminch.minibank.app.db.AccountRepository
import com.daniminch.minibank.app.db.UserRepository
import com.daniminch.minibank.app.model.User
import com.daniminch.minibank.app.service.error.ActionResult
import com.daniminch.minibank.app.service.error.MessageCode
import org.springframework.stereotype.Service

@Service
class UserService(
  val userRepository: UserRepository,
  val accountRepository: AccountRepository
) {

  fun create(user: UserCreateDto): UserDto {
    val entity = userRepository.save(user)
    accountRepository.createDefault(entity.id)
    return entity.toDto()
  }

  fun get(userId: Long): UserDto {
    return userRepository.get(userId).toDto()
  }

  fun getByLogin(login: String): UserDto {
    return userRepository.getByLogin(login).toDto()
  }

  fun deactivate(userId: Long): ActionResult<UserDto> {
    val old = userRepository.get(userId)
    return if (old.status == User.Status.SUSPENDED) {
      MessageCode.USER_ALREADY_SUSPENDED.warnOf(old.toDto())
    } else {
      ActionResult.okResult(
        userRepository.updateStatus(
          id = old.id,
          status = User.Status.SUSPENDED
        ).toDto()
      )
    }
  }
}

private fun User.toDto() = UserDto(
  id = id,
  login = login,
  status = status
)