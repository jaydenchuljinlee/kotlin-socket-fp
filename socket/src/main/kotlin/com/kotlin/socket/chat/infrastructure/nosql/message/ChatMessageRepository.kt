package com.kotlin.socket.chat.infrastructure.nosql.message

import com.kotlin.socket.chat.infrastructure.nosql.message.entity.ChatMessage
import org.springframework.data.cassandra.repository.CassandraRepository
import java.util.*

interface ChatMessageRepository : CassandraRepository<ChatMessage, UUID> {
    fun findTop50ByRoomIdOrderBySentAtDesc(roomId: String): List<ChatMessage>
}