package com.kotlin.socket.chat.infrastructure

import com.kotlin.socket.chat.dto.ChatState
import org.springframework.stereotype.Component

@Component
class InMemoryChatStateStore(): ChatStateStore {
    private val stateMap: MutableMap<String, ChatState> = mutableMapOf()

    override fun getState(roomId: String): ChatState {
        return stateMap[roomId] ?: ChatState()
    }

    override fun updateState(roomId: String, newState: ChatState) {
        stateMap[roomId] = newState
    }
}