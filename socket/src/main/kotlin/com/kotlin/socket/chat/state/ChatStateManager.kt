package com.kotlin.socket.chat.state

import com.kotlin.socket.chat.model.ChatEvent
import org.springframework.web.socket.WebSocketSession

class ChatStateManager(
    private val sessions: Map<String, WebSocketSession> = emptyMap()
) {
    fun handle(event: ChatEvent, session: WebSocketSession): Pair<ChatStateManager, List<Pair<WebSocketSession, String>>> {
        return when (event) {
            is ChatEvent.Join -> {
                val newSessions = sessions + (event.userId to session)
                ChatStateManager(newSessions) to newSessions.values.map { it to "[System] ${event.userId} joined." }
            }

            is ChatEvent.Message -> {
                val toSession = sessions[event.to]
                if (toSession != null) {
                    this to listOf(toSession to "${event.from}: ${event.content}")
                } else {
                    this to emptyList()
                }
            }

            is ChatEvent.Leave -> {
                val newSessions = sessions - event.userId
                ChatStateManager(newSessions) to newSessions.values.map { it to "[System] ${event.userId} left." }
            }
        }
    }
}