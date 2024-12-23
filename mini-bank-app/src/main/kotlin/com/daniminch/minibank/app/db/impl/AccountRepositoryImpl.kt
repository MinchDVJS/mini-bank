package com.daniminch.minibank.app.db.impl

import com.daniminch.minibank.app.db.AccountRepository
import com.daniminch.minibank.app.model.Account
import com.daniminch.minibank.app.service.error.duplicateAccount
import com.daniminch.minibank.app.service.error.notFound
import com.daniminch.minibank.app.utils.KeyLockManager
import org.mapdb.Atomic
import org.mapdb.DB
import org.mapdb.Serializer
import org.springframework.stereotype.Service

@Service
class AccountRepositoryImpl(
  private val db: DB,
  private val accountMap: MutableMap<Long, Account>,
  private val entityIdGenerator: Atomic.Long
) : AccountRepository {

  private val userAccountMapName = "userAccountMap"

  private val uIdxUserId = db.hashMap(userAccountMapName)
    .keySerializer(Serializer.LONG)
    .valueSerializer(Serializer.LONG)
    .createOrOpen()

  private val lock = KeyLockManager<Long>()

  override fun createDefault(userId: Long): Account {
    return lock.locked(userId) {
      if (uIdxUserId.containsKey(userId)) {
        throw duplicateAccount(userId).thrown()
      }
      val id = entityIdGenerator.incrementAndGet()
      val ent = Account(id = id, userId = userId, name = "Default", available = 0.0)
      accountMap[id] = ent
      uIdxUserId[userId] = id
      db.commit()
      ent
    }
  }

  override fun get(accountId: Long): Account {
    return accountMap[accountId]
      ?: throw notFound(accountClassName, accountId).thrown()

  }

  override fun getByUserId(userId: Long): List<Account> {
    return lock.locked(userId) {
      listOf(get(uIdxUserId[userId] ?: throw notFound(userAccountMapName, userId).thrown()))
    }
  }

  private val accountClassName = Account::class.simpleName!!

  override fun changeBalance(accountId: Long, amount: Double): Account {
    val result = accountMap.compute(accountId) { _, v ->
      if (v == null) throw notFound(accountClassName, accountId).thrown()
      v.copy(available = v.available + amount)
    }!!.also {
      db.commit()
    }
    return result
  }

}