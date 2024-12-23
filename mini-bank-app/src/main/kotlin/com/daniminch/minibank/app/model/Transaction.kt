package com.daniminch.minibank.app.model

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.serializer.GroupSerializerObjectArray
import java.time.Instant
import java.util.UUID

data class Transaction(
  val id: UUID,
  val accountId: Long,
  val receiveTs: Instant,
  val info: String,
  val action: Action,
  val amount: Double,
  val completeWithCounterparty: Boolean = false,
  var counterpartyTransactionId: UUID? = null,
  var state: State = State.PENDING
) {

  enum class Action(val code: Short) {
    DEPOSIT(50),
    WITHDRAW(100)
    ;

    companion object {

      private val map = entries.associateBy { it.code }
      fun getByCode(code: Short) = map.getValue(code)
    }
  }

  enum class State(val code: Short) {
    PENDING(100),
    COMPLETED(200),
    COMPLETED_AWAIT_COUNTER(210),
    REJECTED(500);

    companion object {

      private val map = entries.associateBy { it.code }
      fun getByCode(code: Short) = map.getValue(code)
    }
  }

  class Serializer : GroupSerializerObjectArray<Transaction>() {

    override fun serialize(out: DataOutput2, value: Transaction) {
      out.writeUTF(value.id.toString())
      out.writeLong(value.accountId)
      out.writeUTF(value.receiveTs.toString())
      out.writeUTF(value.info)
      out.writeShort(value.action.code.toInt())
      out.writeDouble(value.amount)
      out.writeBoolean(value.completeWithCounterparty)
      out.writeUTF(value.counterpartyTransactionId?.toString() ?: "")
      out.writeShort(value.state.code.toInt())
    }

    override fun deserialize(input: DataInput2, available: Int): Transaction {
      val id = java.util.UUID.fromString(input.readUTF())
      val accountId = input.readLong()
      val receiveTs = Instant.parse(input.readUTF())
      val info = input.readUTF()
      val action = Action.getByCode(input.readShort())
      val amount = input.readDouble()
      val completeWithCounterparty = input.readBoolean()
      val counterpartyTransactionId = input.readUTF().takeUnless { it == "" }?.let {
        java.util.UUID.fromString(it)
      }
      val state = State.getByCode(input.readShort())
      return Transaction(
        id = id,
        accountId = accountId,
        receiveTs = receiveTs,
        info = info,
        action = action,
        amount = amount,
        completeWithCounterparty = completeWithCounterparty,
        counterpartyTransactionId = counterpartyTransactionId,
        state = state
      )
    }

  }
}
