package com.kotlin.socket.chat.controller

import com.kotlin.socket.chat.dto.*
import com.kotlin.socket.chat.error.toMessage
import com.kotlin.socket.chat.model.ChatCommand
import com.kotlin.socket.chat.service.ChatService
import kotlinx.coroutines.runBlocking
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val chatService: ChatService
) {
    @MessageMapping("/chat/join/{roomId}")
    @SendTo("/topic/chatroom/{roomId}")
    fun handleJoin(@DestinationVariable roomId: String, @Payload request: JoinRequest): MessageResponse {
        return runBlocking {
            chatService.executeCommand(ChatCommand.Join(roomId, request.userId))
                .fold(
                    ifLeft = { MessageResponse("[Error] ${it.toMessage()}") },
                    ifRight = { it }
                )
        }
    }


    @MessageMapping("/chat/message/{roomId}")
    @SendTo("/topic/chatroom/{roomId}")
    fun handleMessage(
        @DestinationVariable roomId: String,
        @Payload request: MessageRequest): MessageResponse {
        return runBlocking {
            chatService.executeCommand(ChatCommand.SendMessage(roomId, request.from, request.to, request.content))
                .fold(
                    ifLeft = { MessageResponse("[Error] ${it.toMessage()}") },
                    ifRight = { it }
                )
        }
    }

    @MessageMapping("/chat/leave/{roomId}")
    @SendTo("/topic/chatroom/{roomId}")
    fun handleLeave(
        @DestinationVariable roomId: String,
        @Payload request: LeaveRequest): MessageResponse {
        return runBlocking {
            chatService.executeCommand(ChatCommand.Leave(roomId, request.userId))
                .fold(
                    ifLeft = { MessageResponse("[Error] ${it.toMessage()}") },
                    ifRight = { it }
                )
        }
    }
}