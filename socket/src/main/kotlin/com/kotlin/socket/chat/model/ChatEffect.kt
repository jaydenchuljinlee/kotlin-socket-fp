package com.kotlin.socket.chat.model

sealed class ChatEffect {
    data class Broadcast(val roomId: String, val message: String) : ChatEffect()
    data class PersistMessage(val roomId: String, val from: Long, val content: String) : ChatEffect()
    data class Log(val content: String) : ChatEffect()
    data class JoinUser(val roomId: String, val userId: Long) : ChatEffect()
    data class LeaveUser(val roomId: String, val userId: Long) : ChatEffect()
    data class CacheMessage(val roomId: String, val message: String) : ChatEffect()
}