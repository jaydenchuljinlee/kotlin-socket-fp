package com.kotlin.socket.chat.handler

import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.model.ChatCommand
import com.kotlin.socket.chat.model.ChatEffect

object ChatInterpreter {
    fun interpret(cmd: ChatCommand, state: ChatState): Pair<ChatState, List<ChatEffect>> {
        return when (cmd) {
            is ChatCommand.Join -> {
                val newState = state.copy(activeUsers = state.activeUsers + cmd.userId)
                newState to listOf(
                    ChatEffect.Broadcast(cmd.roomId, "[System] ${cmd.userId} joined."),
                    ChatEffect.Log("Join: ${cmd.userId} in ${cmd.roomId}")
                )
            }
            is ChatCommand.Leave -> {
                val newState = state.copy(activeUsers = state.activeUsers - cmd.userId)
                newState to listOf(
                    ChatEffect.Broadcast(cmd.roomId, "[System] ${cmd.userId} left."),
                    ChatEffect.Log("Leave: ${cmd.userId} from ${cmd.roomId}")
                )
            }
            is ChatCommand.SendMessage -> {
                state to listOf(
                    ChatEffect.Broadcast(cmd.roomId, "${cmd.from}: ${cmd.content}"),
                    ChatEffect.PersistMessage(cmd.roomId, cmd.from, cmd.content),
                    ChatEffect.Log("Message from ${cmd.from} in ${cmd.roomId}")
                )
            }
        }
    }

}