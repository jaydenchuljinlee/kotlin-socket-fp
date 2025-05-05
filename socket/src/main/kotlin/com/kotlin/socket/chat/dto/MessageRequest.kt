package com.kotlin.socket.chat.dto

data class MessageRequest(val from: Long, val to: Long, val content: String)