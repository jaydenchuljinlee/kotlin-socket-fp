package com.kotlin.socket.chat.controller

import com.kotlin.socket.chat.dto.*
import com.kotlin.socket.chat.handler.ChatEventHandler
import com.kotlin.socket.chat.model.ChatEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var state: ChatState = ChatState()

    @MessageMapping("/chat/join")
    @SendTo("/topic/chatroom")
    fun handleJoin(@Payload request: JoinRequest): MessageResponse {
        val (newState, response) = ChatEventHandler.handle(ChatEvent.Join(request.userId), state)
        state = newState
        logger.info("✅ Join request: $request")
        return response
    }

    @MessageMapping("/chat/message")
    @SendTo("/topic/chatroom")
    fun handleMessage(@Payload request: MessageRequest): MessageResponse {
        val (newState, response) = ChatEventHandler.handle(
            ChatEvent.Message(request.from, request.to, request.content), state
        )
        state = newState
        logger.info("📨 Message request: $request")
        return response
    }

    @MessageMapping("/chat/leave")
    @SendTo("/topic/chatroom")
    fun handleLeave(@Payload request: LeaveRequest): MessageResponse {
        val (newState, response) = ChatEventHandler.handle(ChatEvent.Leave(request.userId), state)
        state = newState
        logger.info("👋 Leave request: $request")
        return response
    }

}