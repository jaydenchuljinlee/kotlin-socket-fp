package com.kotlin.socket.chat.dto

import com.kotlin.socket.chat.model.ChatEffect

data class MessageResponse(val content: String) {
    companion object {
        fun from(effects: List<ChatEffect>): MessageResponse {
            val broadcast = effects.filterIsInstance<ChatEffect.Broadcast>().firstOrNull()
            return MessageResponse(broadcast?.message ?: "[System] No broadcast message.")
        }
    }
}

