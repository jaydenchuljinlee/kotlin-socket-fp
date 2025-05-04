package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.infrastructure.ChatHistoryStore
import com.kotlin.socket.chat.infrastructure.ChatStateStore
import com.kotlin.socket.chat.model.ChatEffect
import org.springframework.stereotype.Component

@Component
class ChatEffectExecutor(
    private val store: ChatStateStore,
    private val history: ChatHistoryStore

) {
    suspend fun runEffects(effects: List<ChatEffect>) {
        for (effect in effects) {
            when (effect) {
                is ChatEffect.Broadcast -> println("📢 [${effect.roomId}] ${effect.message}")
                is ChatEffect.PersistMessage -> history.saveMessage(effect.roomId, effect.from, effect.content)
                is ChatEffect.Log -> println("🪵 LOG: ${effect.content}")
            }
        }
    }
}