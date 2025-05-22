package com.kotlin.socket.chat.dto

import com.kotlin.socket.chat.model.ChatEffect

data class TypingResponse(val userId: Long) {
    companion object {
        fun from(effects: List<ChatEffect>): TypingResponse {
            val typing = effects.filterIsInstance<ChatEffect.Typing>().firstOrNull()
            return TypingResponse(typing?.userId ?: -1L)
        }
    }
}