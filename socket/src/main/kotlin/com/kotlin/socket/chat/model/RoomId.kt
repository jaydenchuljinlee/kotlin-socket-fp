package com.kotlin.socket.chat.model

@JvmInline
value class RoomId(val value: String) {
    init {
        require(value.isNotBlank()) { "RoomId cannot be blank" }
    }

    override fun toString(): String = value
} 