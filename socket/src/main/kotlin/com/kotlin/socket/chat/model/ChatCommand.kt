package com.kotlin.socket.chat.model

sealed class ChatCommand {
    data class Join(val roomId: String, val userId: Long) : ChatCommand()
    data class SendMessage(val roomId: String, val from: Long, val to: Long, val content: String) : ChatCommand()
    data class Leave(val roomId: String, val userId: Long) : ChatCommand()
    data class Typing(val roomId: String, val userId: Long) : ChatCommand()

    fun findRoomId(): String = when (this) {
        is Join -> this.roomId
        is SendMessage -> this.roomId
        is Leave -> this.roomId
        is Typing -> this.roomId
    }
}


