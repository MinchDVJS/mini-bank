package com.daniminch.minibank.app.config

import com.daniminch.minibank.app.config.properties.ApplicationProperties
import com.daniminch.minibank.app.model.Account
import com.daniminch.minibank.app.model.Transaction
import com.daniminch.minibank.app.model.User
import org.mapdb.Atomic
import org.mapdb.DB
import org.mapdb.DBMaker.fileDB
import org.mapdb.DBMaker.memoryDB
import org.mapdb.Serializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID


@Configuration
class MapDBConfig {

  @Bean
  fun mapDb(
    appProperties: ApplicationProperties
  ): DB {
    val db = if (appProperties.persistDb) {
      fileDB("data.db")
        .fileMmapEnable()
    } else {
      memoryDB()
    }
    return db
      .transactionEnable()
      .make()
  }

  @Bean
  fun entityIdGenerator(db: DB): Atomic.Long {
    return db.atomicLong("entityIdGenerator").createOrOpen()
  }

  @Bean
  fun userMap(db: DB): MutableMap<Long, User> {
    return db.hashMap("userMap")
      .keySerializer(Serializer.LONG)
      .valueSerializer(User.Serializer())
      .createOrOpen()
  }

  @Bean
  fun accountMap(db: DB): MutableMap<Long, Account> {
    return db.hashMap("accountMap")
      .keySerializer(Serializer.LONG)
      .valueSerializer(Account.Serializer())
      .createOrOpen()
  }

  @Bean
  fun transactionMap(db: DB): MutableMap<UUID, Transaction> {
    return db.hashMap("transactionMap")
      .keySerializer(Serializer.UUID)
      .valueSerializer(Transaction.Serializer())
      .createOrOpen()
  }
}