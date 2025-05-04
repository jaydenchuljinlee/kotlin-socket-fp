package com.kotlin.socket.chat.controller

import com.kotlin.socket.chat.dto.*
import com.kotlin.socket.chat.handler.ChatEventHandler
import com.kotlin.socket.chat.infrastructure.ChatStateStore
import com.kotlin.socket.chat.model.ChatEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val chatStateStore: ChatStateStore
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @MessageMapping("/chat/join")
    @SendTo("/topic/chatroom/{roomId}")
    fun handleJoin(
        @DestinationVariable roomId: String,
        @Payload request: JoinRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, response) = ChatEventHandler.handle(ChatEvent.Join(request.userId), current)
        chatStateStore.updateState(roomId, newState)
        logger.info("✅ Join request: $request")
        return response
    }

    @MessageMapping("/chat/message")
    @SendTo("/topic/chatroom/{roomId}")
    fun handleMessage(
        @DestinationVariable roomId: String,
        @Payload request: MessageRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, response) = ChatEventHandler.handle(
            ChatEvent.Message(request.from, request.to, request.content), current
        )
        chatStateStore.updateState(roomId, newState)
        logger.info("📨 Message request: $request")
        return response
    }

    @MessageMapping("/chat/leave/{roomId}")
    @SendTo("/topic/chatroom")
    fun handleLeave(
        @DestinationVariable roomId: String,
        @Payload request: LeaveRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, response) = ChatEventHandler.handle(ChatEvent.Leave(request.userId), current)
        chatStateStore.updateState(roomId, newState)
        logger.info("👋 Leave request: $request")
        return response
    }

}