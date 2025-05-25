package com.kotlin.socket.chat.infrastructure.inmemory.store

interface ChatHistoryStore {
    fun saveMessage(roomId: String, from: Long, content: String)
}