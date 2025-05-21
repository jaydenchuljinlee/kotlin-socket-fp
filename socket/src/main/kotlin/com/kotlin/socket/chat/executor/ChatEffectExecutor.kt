package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class ChatEffectExecutor(
    private val broadcastExecutor: BroadcastEffectExecutor,
    private val persistMessageExecutor: PersistMessageEffectExecutor,
    private val userStateEffectExecutor: UserStateEffectExecutor,
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun runEffects(effects: List<ChatEffect>): Flow<Either<ChatError, Unit>> = flow {
        for (effect in effects) {
            val result: Either<ChatError, Unit> = try {
                when (effect) {
                    is ChatEffect.Broadcast -> {
                        logger.info("📡 Broadcasting: $effect")
                        broadcastExecutor.execute(effect)
                    }
                    is ChatEffect.PersistMessage -> {
                        logger.info("💾 Persisting message: $effect")
                        persistMessageExecutor.execute(effect)
                    }
                    is ChatEffect.JoinUser -> {
                        logger.info("👤 Joining user: $effect")
                        userStateEffectExecutor.execute(effect)
                    }
                    is ChatEffect.LeaveUser -> {
                        logger.info("👋 Leaving user: $effect")
                        userStateEffectExecutor.execute(effect)
                    }
                    is ChatEffect.CacheMessage -> {
                        logger.info("🧠 Caching message: $effect")
                        cacheToRedis(effect)
                    }
                    is ChatEffect.Log -> {
                        logger.info("🪵 LOG: ${effect.content}")
                        Either.Right(Unit)
                    }
                }
            } catch (e: Exception) {
                logger.error("❌ Effect 처리 중 예외: ${e.message}", e)
                Either.Left(ChatError.InvalidMessage("Effect failure: ${e.message}"))
            }
            emit(result)
        }
    }


    private fun cacheToRedis(effect: ChatEffect.CacheMessage): Either<ChatError, Unit> = try {
        logger.info("🪵 Cache: Hello")
        val key = "room:${effect.roomId}:messages"
        redisTemplate.opsForList().leftPush(key, effect.message)
        redisTemplate.opsForList().trim(key, 0, 99) // 최대 100개 유지
        Either.Right(Unit)
    } catch (e: Exception) {
        Either.Left(ChatError.InvalidMessage("Redis Cache Error: ${e.message}"))
    }

}