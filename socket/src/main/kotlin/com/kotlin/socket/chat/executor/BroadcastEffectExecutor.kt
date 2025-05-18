package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BroadcastEffectExecutor : EffectExecutor<ChatEffect.Broadcast> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun execute(effect: ChatEffect.Broadcast): Either<ChatError, Unit> = withContext(Dispatchers.IO) {
        try {
            logger.info("📢 [${effect.roomId}] ${effect.message}")
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ChatError.InvalidMessage("Failed to broadcast message: ${e.message}"))
        }
    }
} 