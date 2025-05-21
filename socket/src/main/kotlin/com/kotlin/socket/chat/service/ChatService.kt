package com.kotlin.socket.chat.service

import arrow.core.Either
import com.kotlin.socket.chat.dto.MessageResponse
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.executor.ChatEffectExecutor
import com.kotlin.socket.chat.handler.ChatInterpreter
import com.kotlin.socket.chat.infrastructure.inmemory.store.ChatStateStore
import com.kotlin.socket.chat.model.ChatCommand
import com.kotlin.socket.chat.model.RoomId
import com.kotlin.socket.common.lock.RedissonLockSupporter
import kotlinx.coroutines.flow.first
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ChatService(
    @Qualifier("redisChatStateStore")
    private val stateStore: ChatStateStore,
    private val redissonLockSupporter: RedissonLockSupporter,
    private val executor: ChatEffectExecutor
) {
    suspend fun executeCommand(cmd: ChatCommand): Either<ChatError, MessageResponse> {
        val roomId = RoomId(cmd.findRoomId())
        return redissonLockSupporter.withLockSuspend("lock:room:$roomId") {
            val currentState = stateStore.getState(roomId)
            ChatInterpreter.interpret(cmd, currentState)
                .map { (newState, effects) ->
                    stateStore.updateState(roomId, newState)
                    executor.runEffects(effects).first() // 첫 번째 효과 실행 결과만 사용
                    MessageResponse.from(effects)
                }
        }
    }
}