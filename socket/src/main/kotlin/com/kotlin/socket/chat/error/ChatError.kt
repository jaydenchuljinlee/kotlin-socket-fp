package com.kotlin.socket.chat.error

sealed class ChatError {
    data class UserNotFound(val userId: Long) : ChatError()
    data class UserAlreadyInRoom(val userId: Long) : ChatError()
    data class InvalidMessage(val reason: String) : ChatError()
    data class RedisError(val reason: String) : ChatError()
}

fun ChatError.toMessage(): String = when (this) {
    is ChatError.UserAlreadyInRoom -> "User $userId is already in the room."
    is ChatError.UserNotFound -> "User $userId is not in the room."
    is ChatError.InvalidMessage -> "Invalid message: $reason"
    is ChatError.RedisError -> "Redis error: $reason"
}
