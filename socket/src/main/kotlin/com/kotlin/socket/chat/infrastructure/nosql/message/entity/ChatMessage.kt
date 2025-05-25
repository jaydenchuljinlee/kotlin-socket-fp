package com.kotlin.socket.chat.infrastructure.nosql.message.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("chat_messages")
data class ChatMessage(
    @PrimaryKeyColumn(name = "room_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val roomId: String,

    @PrimaryKeyColumn(name = "sent_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val sentAt: Instant,

    @PrimaryKeyColumn(name = "message_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val messageId: UUID = UUID.randomUUID(),

    @Column("from_user")
    val fromUser: Long,

    @Column("to_user")
    val toUser: Long,

    val content: String
)