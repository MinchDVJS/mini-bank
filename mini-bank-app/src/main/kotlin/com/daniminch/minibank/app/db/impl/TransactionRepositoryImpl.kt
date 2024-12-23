package com.daniminch.minibank.app.db.impl

import com.daniminch.minibank.app.db.TransactionRepository
import com.daniminch.minibank.app.model.Transaction
import com.daniminch.minibank.app.service.error.notFound
import mu.KotlinLogging
import org.mapdb.DB
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import org.mapdb.serializer.GroupSerializerObjectArray
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TransactionRepositoryImpl(
  private val db: DB,
  private val transactionMap: MutableMap<UUID, Transaction>
) : TransactionRepository {

  private val accountTransactionMapName = "transactionAccountMap"

  private val accountTransactionMap = db.hashMap(accountTransactionMapName)
    .keySerializer(Serializer.LONG)
    .valueSerializer(UUIDSerializer())
    .createOrOpen()

  private class UUIDSerializer : GroupSerializerObjectArray<Set<UUID>>() {

    override fun serialize(out: DataOutput2, value: Set<UUID>) {
      out.packInt(value.size) // First write the size of the list
      value.forEach { uuid ->
        out.writeUTF(uuid.toString()) // Write each UUID as a string
      }
    }

    override fun deserialize(input: DataInput2, available: Int): Set<UUID> {
      val size = input.unpackInt() // Read the size of the list
      val set = mutableSetOf<UUID>()
      repeat(size) {
        set.add(java.util.UUID.fromString(input.readUTF())) // Read each UUID
      }
      return set
    }

  }

  private val logger = KotlinLogging.logger {}

  override fun save(transaction: Transaction): Transaction {
    val uuid = transaction.id
    transactionMap[uuid] = transaction
    accountTransactionMap.compute(transaction.accountId) { k, v ->
      v?.let { v.plus(uuid) } ?: setOf(uuid)
    }
    db.commit()
    return transaction
  }

  private val transactionClassName = Transaction::class.simpleName!!

  override fun get(transactionId: UUID): Transaction {
    return transactionMap[transactionId] ?: throw notFound(transactionClassName, transactionId).thrown()
  }

  override fun getByAccount(accountId: Long): List<Transaction> {
    return accountTransactionMap[accountId]
      ?.mapNotNull { transactionMap[it] }
      ?.sortedByDescending { it.receiveTs }
      ?: emptyList()
  }
}