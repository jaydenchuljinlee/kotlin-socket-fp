package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.infrastructure.ChatHistoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PersistMessageEffectExecutor(
    private val history: ChatHistoryStore
) : EffectExecutor<ChatEffect.PersistMessage> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun execute(effect: ChatEffect.PersistMessage): Either<ChatError, Unit> = withContext(Dispatchers.IO) {
        try {
            history.saveMessage(effect.roomId, effect.from, effect.content)
            logger.info("💾 저장됨 [${effect.roomId}] ${effect.from}: ${effect.content}")
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ChatError.InvalidMessage("Failed to persist message: ${e.message}"))
        }
    }
} 