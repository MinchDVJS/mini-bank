package com.daniminch.minibank.app.db.impl

import com.daniminch.minibank.app.controller.dto.UserCreateDto
import com.daniminch.minibank.app.db.UserRepository
import com.daniminch.minibank.app.model.User
import com.daniminch.minibank.app.service.error.duplicateLogin
import com.daniminch.minibank.app.service.error.notFound
import com.daniminch.minibank.app.utils.KeyLockManager
import org.mapdb.Atomic
import org.mapdb.DB
import org.mapdb.Serializer
import org.springframework.stereotype.Service

@Service
class UserRepositoryImpl(
  private val db: DB,
  private val userMap: MutableMap<Long, User>,
  private val entityIdGenerator: Atomic.Long
) : UserRepository {

  private val userLoginMapName = "userLoginMap"
  private val uIdxLogin = db.hashMap(userLoginMapName)
    .keySerializer(Serializer.STRING_ASCII)
    .valueSerializer(Serializer.LONG)
    .createOrOpen()

  val lock = KeyLockManager<String>()

  override fun save(user: UserCreateDto): User {
    val login = user.login.lowercase()
    val result = lock.locked(login) {
      if (uIdxLogin.containsKey(login)) {
        throw duplicateLogin(login).thrown()
      }
      val id = entityIdGenerator.incrementAndGet()
      val ent = User(id = id, login = login, status = User.Status.ACTIVE)
      userMap[id] = ent
      uIdxLogin[login] = id
      db.commit()
      ent
    }
    return result
  }

  override fun get(userId: Long): User {
    return userMap[userId] ?: throw notFound(userClassName, userId).thrown()
  }

  override fun getByLogin(login: String): User {
    return lock.locked(login) {
      val userId = uIdxLogin[login] ?: throw notFound(userLoginMapName, login).thrown()
      userMap[userId] ?: throw notFound(userClassName, userId).thrown()
    }
  }

  private val userClassName = User::class.simpleName!!

  override fun updateStatus(id: Long, status: User.Status): User {
    val result = userMap.compute(id) { _, v ->
      if (v == null) throw notFound(userClassName, id).thrown()
      v.copy(status = status)
    }!!.also {
      db.commit()
    }
    return result
  }
}