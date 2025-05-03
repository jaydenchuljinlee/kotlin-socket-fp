package com.kotlin.socket.chat.dto

data class ChatState(val activeUsers: Set<Long> = emptySet())