package com.kotlin.socket.chat.infrastructure

import org.springframework.stereotype.Component

@Component
class InMemoryChatHistoryStore : ChatHistoryStore {
    private val history = mutableListOf<Triple<String, Long, String>>() // (roomId, userId, content)

    override fun saveMessage(roomId: String, from: Long, content: String) {
        history += Triple(roomId, from, content)
        println("💾 저장됨 [$roomId] $from: $content")
    }

    fun getHistory(): List<Triple<String, Long, String>> = history.toList()
}
