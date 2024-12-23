package com.daniminch.minibank.app.db

import com.daniminch.minibank.app.model.Account

interface AccountRepository {

  fun createDefault(userId: Long): Account
  fun get(accountId: Long): Account
  fun getByUserId(userId: Long): List<Account>
  fun changeBalance(accountId: Long, amount: Double): Account
}