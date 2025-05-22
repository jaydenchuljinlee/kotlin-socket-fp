package com.kotlin.socket.chat.executor

import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatEffect
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TypingEffectExecutor(
    private val redisTemplate: StringRedisTemplate
): EffectExecutor<ChatEffect.Typing> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun execute(effect: ChatEffect.Typing): Either<ChatError, Unit> {
        return try {
            val key = "room:${effect.roomId}:typing"
            redisTemplate.opsForSet().add(key, effect.userId.toString())
            redisTemplate.expire(key, Duration.ofSeconds(5)) // TTL 설정
            logger.info("✍️ ${effect.userId} is typing in ${effect.roomId}")
            Either.Right(Unit)
        } catch (e: Exception) {
            logger.warn("Redis typing 상태 업데이트 실패: ${e.message}")
            Either.Left(ChatError.InvalidMessage("Typing 상태 저장 실패"))
        }
    }
}