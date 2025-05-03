package com.kotlin.socket.chat.handler

import com.kotlin.socket.chat.model.ChatEvent
import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.dto.MessageResponse

object ChatEventHandler {
    fun handle(event: ChatEvent, state: ChatState): Pair<ChatState, MessageResponse> =
        when (event) {
            is ChatEvent.Join -> {
                val newState = state.copy(activeUsers = state.activeUsers + event.userId)
                newState to MessageResponse("[System] User ${event.userId} joined.")
            }

            is ChatEvent.Leave -> {
                val newState = state.copy(activeUsers = state.activeUsers - event.userId)
                newState to MessageResponse("[System] User ${event.userId} left.")
            }

            is ChatEvent.Message -> {
                state to MessageResponse("${event.from}: ${event.content}")
            }
        }
}