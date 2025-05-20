package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatEffectExecutor(
    private val broadcastExecutor: BroadcastEffectExecutor,
    private val persistMessageExecutor: PersistMessageEffectExecutor,
    private val userStateEffectExecutor: UserStateEffectExecutor
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun runEffects(effects: List<ChatEffect>): Flow<Either<ChatError, Unit>> = flow {
        coroutineScope {
            effects.forEach { effect ->
                val result: Either<ChatError, Unit> = when (effect) {
                    is ChatEffect.Broadcast -> async { broadcastExecutor.execute(effect) }.await()
                    is ChatEffect.PersistMessage -> async { persistMessageExecutor.execute(effect) }.await()
                    is ChatEffect.JoinUser, is ChatEffect.LeaveUser -> userStateEffectExecutor.execute(effect)
                    is ChatEffect.Log -> {
                        logger.info("🪵 LOG: ${effect.content}")
                        Either.Right(Unit)
                    }
                }
                emit(result)
            }
        }
    }
}