package com.kotlin.socket.common.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kotlin.socket.chat.model.ChatEvent
import com.kotlin.socket.chat.state.ChatStateManager
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.atomic.AtomicReference

class ChatWebSocketHandler: TextWebSocketHandler() {
    private val state = AtomicReference(ChatStateManager())
    private val mapper = jacksonObjectMapper()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val event = runCatching { mapper.readValue<ChatEvent>(message.payload) }.getOrNull() ?: return
        val (newState, outgoing) = state.get().handle(event, session)
        state.set(newState)
        outgoing.forEach { (target, msg) ->
            if (target.isOpen) target.sendMessage(TextMessage(msg))
        }
    }
}