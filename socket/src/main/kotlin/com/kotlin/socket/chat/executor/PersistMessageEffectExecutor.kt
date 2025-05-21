package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.infrastructure.inmemory.store.ChatHistoryStore
import com.kotlin.socket.chat.infrastructure.nosql.message.ChatMessageRepository
import com.kotlin.socket.chat.infrastructure.nosql.message.entity.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class PersistMessageEffectExecutor(
    private val chatMessageRepository: ChatMessageRepository,
    // private val history: ChatHistoryStore
) : EffectExecutor<ChatEffect.PersistMessage> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun execute(effect: ChatEffect.PersistMessage): Either<ChatError, Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = ChatMessage(
                roomId = effect.roomId,
                sentAt = Instant.now(),
                messageId = UUID.randomUUID(),
                fromUser = effect.from,
                toUser = 0, // broadcast일 경우
                content = effect.content
            )
            chatMessageRepository.save(entity)
            // history.saveMessage(effect.roomId, effect.from, effect.content)
            logger.info("💾 저장됨 [${effect.roomId}] ${effect.from}: ${effect.content}")
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ChatError.InvalidMessage("Failed to persist message: ${e.message}"))
        }
    }
} 