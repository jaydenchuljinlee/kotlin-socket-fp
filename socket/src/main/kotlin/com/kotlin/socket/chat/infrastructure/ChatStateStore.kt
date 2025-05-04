package com.kotlin.socket.chat.infrastructure

import com.kotlin.socket.chat.dto.ChatState

interface ChatStateStore {
    fun getState(roomId: String): ChatState
    fun updateState(roomId: String, newState: ChatState)

}