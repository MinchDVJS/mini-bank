package com.daniminch.minibank.app.model

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.serializer.GroupSerializerObjectArray

data class Account(
  val id: Long,
  val userId: Long,
  val name: String,
  val available: Double
) {

  class Serializer : GroupSerializerObjectArray<Account>() {

    override fun serialize(out: DataOutput2, value: Account) {
      out.writeLong(value.id)
      out.writeLong(value.userId)
      out.writeUTF(value.name)
      out.writeDouble(value.available)
    }

    override fun deserialize(input: DataInput2, available: Int): Account {
      val id = input.readLong()
      val user = input.readLong()
      val name = input.readUTF()
      val availableBalance = input.readDouble()
      return Account(id, user, name, availableBalance)
    }
  }
}