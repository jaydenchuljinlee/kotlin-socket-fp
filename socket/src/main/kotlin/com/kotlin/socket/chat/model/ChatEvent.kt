package com.kotlin.socket.chat.model

sealed class ChatEvent {
    data class Join(val userId: String) : ChatEvent()
    data class Message(val from: String, val to: String, val content: String) : ChatEvent()
    data class Leave(val userId: String) : ChatEvent()
}