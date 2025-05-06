package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import com.kotlin.socket.chat.error.ChatError

interface EffectExecutor<T : ChatEffect> {
    suspend fun execute(effect: T): Either<ChatError, Unit>
} 