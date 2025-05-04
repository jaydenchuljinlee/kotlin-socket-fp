package com.kotlin.socket.chat.controller

import com.kotlin.socket.chat.dto.*
import com.kotlin.socket.chat.executor.ChatEffectExecutor
import com.kotlin.socket.chat.handler.ChatInterpreter
import com.kotlin.socket.chat.infrastructure.ChatStateStore
import com.kotlin.socket.chat.model.ChatCommand
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val chatStateStore: ChatStateStore,
    private val chatEffectExecutor: ChatEffectExecutor
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @MessageMapping("/chat/join")
    @SendTo("/topic/chatroom/{roomId}")
    suspend fun handleJoin(
        @DestinationVariable roomId: String,
        @Payload request: JoinRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, effects) = ChatInterpreter.interpret(
            ChatCommand.Join(roomId, request.userId),
            current
        )
        chatStateStore.updateState(roomId, newState)
        logger.info("✅ Join request: $request")
        chatEffectExecutor.runEffects(effects)
        return MessageResponse("[System] ${request.userId} joined.")
    }

    @MessageMapping("/chat/message")
    @SendTo("/topic/chatroom/{roomId}")
    suspend fun handleMessage(
        @DestinationVariable roomId: String,
        @Payload request: MessageRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, effects) = ChatInterpreter.interpret(
            ChatCommand.SendMessage(roomId, request.from, request.to, request.content),
            current
        )
        chatStateStore.updateState(roomId, newState)
        logger.info("📨 Message request: $request")
        chatEffectExecutor.runEffects(effects)
        return MessageResponse("${request.from}: ${request.content}")
    }

    @MessageMapping("/chat/leave/{roomId}")
    @SendTo("/topic/chatroom")
    suspend fun handleLeave(
        @DestinationVariable roomId: String,
        @Payload request: LeaveRequest): MessageResponse {
        val current = chatStateStore.getState(roomId)
        val (newState, effects) = ChatInterpreter.interpret(
            ChatCommand.Leave(roomId, request.userId),
            current
        )
        chatStateStore.updateState(roomId, newState)
        logger.info("👋 Leave request: $request")
        chatEffectExecutor.runEffects(effects)
        return MessageResponse("[System] ${request.userId} left.")
    }

}