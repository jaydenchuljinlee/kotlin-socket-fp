package com.kotlin.socket.chat.handler

import arrow.core.Either
import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatCommand
import com.kotlin.socket.chat.model.ChatEffect

import arrow.core.*

object ChatInterpreter {
    fun interpret(cmd: ChatCommand, state: ChatState): Either<ChatError, Pair<ChatState, List<ChatEffect>>> {
        return when (cmd) {
            is ChatCommand.Join -> {
                if (state.activeUsers.contains(cmd.userId)) {
                    ChatError.UserAlreadyInRoom(cmd.userId).left()
                } else {
                    val newState = state.copy(activeUsers = state.activeUsers + cmd.userId)
                    (newState to listOf(
                        ChatEffect.Broadcast(cmd.roomId, "[System] ${cmd.userId} joined."),
                        ChatEffect.JoinUser(cmd.roomId, cmd.userId),
                        ChatEffect.Log("Join: ${cmd.userId} in ${cmd.roomId}")
                    )).right()
                }
            }

            is ChatCommand.Leave -> {
                if (!state.activeUsers.contains(cmd.userId)) {
                    ChatError.UserNotFound(cmd.userId).left()
                } else {
                    val newState = state.copy(activeUsers = state.activeUsers - cmd.userId)
                    (newState to listOf(
                        ChatEffect.Broadcast(cmd.roomId, "[System] ${cmd.userId} left."),
                        ChatEffect.LeaveUser(cmd.roomId, cmd.userId),
                        ChatEffect.Log("Leave: ${cmd.userId} from ${cmd.roomId}")
                    )).right()
                }
            }

            is ChatCommand.SendMessage -> {
                if (cmd.content.isBlank()) {
                    ChatError.InvalidMessage("Message is blank").left()
                } else {
                    val message = "${cmd.from}: ${cmd.content}"
                    (state to listOf(
                        ChatEffect.Broadcast(cmd.roomId, "${cmd.from}: ${cmd.content}"),
                        ChatEffect.PersistMessage(cmd.roomId, cmd.from, cmd.content),
                        ChatEffect.Log("Message from ${cmd.from} in ${cmd.roomId}"),
                        ChatEffect.CacheMessage(cmd.roomId, message)
                    )).right()
                }
            }
        }
    }
}