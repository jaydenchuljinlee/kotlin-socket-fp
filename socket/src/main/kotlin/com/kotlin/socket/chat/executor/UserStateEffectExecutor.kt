package com.kotlin.socket.chat.executor

import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatEffect
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserStateEffectExecutor(
    private val redisTemplate: StringRedisTemplate
) {
    fun execute(effect: ChatEffect): Either<ChatError, Unit> {
        return try {
            when (effect) {
                is ChatEffect.JoinUser -> {
                    redisTemplate.opsForSet().add("room:${effect.roomId}:users", effect.userId.toString())
                }
                is ChatEffect.LeaveUser -> {
                    redisTemplate.opsForSet().remove("room:${effect.roomId}:users", effect.userId.toString())
                }
                else -> return Either.Left(ChatError.InvalidMessage("Unsupported effect: $effect"))
            }
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(ChatError.RedisError(e.message ?: "unknown"))
        }
    }
}