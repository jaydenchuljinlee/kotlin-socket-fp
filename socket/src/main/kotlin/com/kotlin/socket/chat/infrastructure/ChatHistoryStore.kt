package com.kotlin.socket.chat.infrastructure

interface ChatHistoryStore {
    fun saveMessage(roomId: String, from: Long, content: String)
}