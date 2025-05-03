package com.kotlin.socket.chat.model

sealed class ChatEvent {
    data class Join(val userId: Long) : ChatEvent()
    data class Message(val from: Long, val to: Long, val content: String) : ChatEvent()
    data class Leave(val userId: Long) : ChatEvent()
}