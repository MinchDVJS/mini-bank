package com.daniminch.minibank.app.model

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.serializer.GroupSerializerObjectArray

data class User(
  val id: Long,
  val login: String,
  val status: Status
) {

  enum class Status(val statusCode: Short) {
    ACTIVE(100),
    SUSPENDED(400);

    companion object {

      private val map = entries.associateBy { it.statusCode }
      fun byCode(code: Short): Status = map.getValue(code)
    }
  }

  class Serializer : GroupSerializerObjectArray<User>() {

    override fun serialize(out: DataOutput2, value: User) {
      out.writeLong(value.id)
      out.writeUTF(value.login)
      out.writeShort(value.status.statusCode.toInt())
    }

    override fun deserialize(input: DataInput2, available: Int): User {
      val id = input.readLong()
      val name = input.readUTF()
      val code = Status.byCode(input.readShort())
      return User(id, name, code)
    }

  }
}
