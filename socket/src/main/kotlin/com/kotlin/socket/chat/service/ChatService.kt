package com.kotlin.socket.chat.service

import arrow.core.Either
import com.kotlin.socket.chat.dto.MessageResponse
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.executor.ChatEffectExecutor
import com.kotlin.socket.chat.handler.ChatInterpreter
import com.kotlin.socket.chat.infrastructure.ChatStateStore
import com.kotlin.socket.chat.model.ChatCommand
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val stateStore: ChatStateStore,
    private val executor: ChatEffectExecutor
) {
    suspend fun executeCommand(cmd: ChatCommand): Either<ChatError, MessageResponse> {
        val currentState = stateStore.getState(cmd.findRoomId())
        return ChatInterpreter.interpret(cmd, currentState)
            .map { (newState, effects) ->
                stateStore.updateState(cmd.findRoomId(), newState)
                executor.runEffects(effects)
                MessageResponse.from(effects)
            }
    }
}