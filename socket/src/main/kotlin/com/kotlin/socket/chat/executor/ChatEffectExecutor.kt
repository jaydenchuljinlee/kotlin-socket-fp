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
    private val persistMessageExecutor: PersistMessageEffectExecutor
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun runEffects(effects: List<ChatEffect>): Flow<Either<ChatError, Unit>> = flow {
        coroutineScope {
            effects.forEach { effect ->
                when (effect) {
                    is ChatEffect.Broadcast -> {
                        val result = async { broadcastExecutor.execute(effect) }
                        emit(result.await())
                    }
                    is ChatEffect.PersistMessage -> {
                        val result = async { persistMessageExecutor.execute(effect) }
                        emit(result.await())
                    }
                    is ChatEffect.Log -> {
                        logger.info("🪵 LOG: ${effect.content}")
                        emit(Either.Right(Unit))
                    }
                }
            }
        }
    }
}