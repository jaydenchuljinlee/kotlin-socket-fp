package com.kotlin.socket.chat.controller

import com.kotlin.socket.chat.dto.JoinRequest
import com.kotlin.socket.chat.dto.LeaveRequest
import com.kotlin.socket.chat.dto.MessageRequest
import com.kotlin.socket.chat.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @MessageMapping("/chat/join")
    @SendTo("/topic/chatroom")
    fun handleJoin(@Payload request: JoinRequest): MessageResponse {
        logger.info("✅ Join request: $request")
        return MessageResponse("[System] User ${request.userId} joined.")
    }

    @MessageMapping("/chat/message")
    @SendTo("/topic/chatroom")
    fun handleMessage(@Payload request: MessageRequest): MessageResponse {
        logger.info("📨 Message request: $request")
        return MessageResponse("${request.from}: ${request.content}")
    }

    @MessageMapping("/chat/leave")
    @SendTo("/topic/chatroom")
    fun handleLeave(@Payload request: LeaveRequest): MessageResponse {
        logger.info("👋 Leave request: $request")
        return MessageResponse("[System] User ${request.userId} left.")
    }

}