package com.kotlin.socket.chat.service

import arrow.core.Either
import com.kotlin.socket.chat.dto.MessageResponse
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.error.toMessage
import com.kotlin.socket.chat.executor.ChatEffectExecutor
import com.kotlin.socket.chat.handler.ChatInterpreter
import com.kotlin.socket.chat.infrastructure.inmemory.store.ChatStateStore
import com.kotlin.socket.chat.model.ChatCommand
import com.kotlin.socket.chat.model.RoomId
import com.kotlin.socket.common.lock.RedissonLockSupporter
import kotlinx.coroutines.flow.first
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ChatService(
    @Qualifier("redisChatStateStore")
    private val stateStore: ChatStateStore,
    private val redissonLockSupporter: RedissonLockSupporter,
    private val executor: ChatEffectExecutor
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun executeCommand(cmd: ChatCommand): Either<ChatError, MessageResponse> {
        val roomId = RoomId(cmd.findRoomId())
        return redissonLockSupporter.withLockSuspend("lock:room:$roomId") {
            val currentState = stateStore.getState(roomId)
            ChatInterpreter.interpret(cmd, currentState)
                .map { (newState, effects) ->
                    stateStore.updateState(roomId, newState)
                    executor.runEffects(effects).collect { result ->
                        // result: Either<ChatError, Unit>
                        result.fold(
                            ifLeft = { error -> logger.warn("Effect error: ${error.toMessage()}") },
                            ifRight = { /* 성공 시 아무것도 안 해도 됨 */ }
                        )
                    }
                    MessageResponse.from(effects)
                }
        }
    }
}