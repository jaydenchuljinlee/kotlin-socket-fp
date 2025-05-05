package com.kotlin.socket.chat.handler

import com.kotlin.socket.chat.model.ChatEvent
import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.dto.MessageResponse
import com.kotlin.socket.chat.model.ChatEffect

import arrow.core.*
import com.kotlin.socket.chat.error.ChatError

object ChatEventHandler {
//    fun handle(event: ChatEvent, state: ChatState): Either<ChatError, Pair<ChatState, List<ChatEffect>>> {
//        return when (event) {
//            is ChatEvent.Join -> {
//                val newState = state.copy(activeUsers = state.activeUsers + event.userId)
//                (newState to listOf(
//                    ChatEffect.Broadcast(event.roomId, "[System] User ${event.userId} joined."),
//                    ChatEffect.Log("Join event for ${event.userId}")
//                )).right()
//            }
//
//            is ChatEvent.Leave -> {
//                val newState = state.copy(activeUsers = state.activeUsers - event.userId)
//                (newState to listOf(
//                    ChatEffect.Broadcast(event.roomId, "[System] User ${event.userId} left."),
//                    ChatEffect.Log("Leave event for ${event.userId}")
//                )).right()
//            }
//
//            is ChatEvent.Message -> {
//                (state to listOf(
//                    ChatEffect.Broadcast(event.roomId, "${event.from}: ${event.content}"),
//                    ChatEffect.PersistMessage(event.roomId, event.from, event.content),
//                    ChatEffect.Log("Message event from ${event.from}")
//                )).right()
//            }
//        }
//
//    }
}