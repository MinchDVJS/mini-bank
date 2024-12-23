package com.daniminch.minibank.app.controller

import com.daniminch.minibank.app.controller.dto.AppResponse
import com.daniminch.minibank.app.controller.dto.UserCreateDto
import com.daniminch.minibank.app.controller.dto.UserDto
import com.daniminch.minibank.app.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserController(
  private val userService: UserService
) {

  @PostMapping
  fun create(@RequestBody user: UserCreateDto): ResponseEntity<AppResponse<UserDto>> {
    val result = userService.create(user)
    return AppResponse.ok(result)
  }

  @GetMapping("{userId}")
  fun getUser(@PathVariable userId: Long): ResponseEntity<AppResponse<UserDto>> {
    val result = userService.get(userId)
    return AppResponse.ok(result)
  }

  @PostMapping("{userId}/deactivate")
  fun deactivate(@PathVariable userId: Long): ResponseEntity<AppResponse<UserDto>> {
    val result = userService.deactivate(userId)
    return AppResponse.fromResult(result)
  }
}