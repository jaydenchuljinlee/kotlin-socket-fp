package com.kotlin.socket.chat.infrastructure

import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.model.RoomId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryChatStateStore : ChatStateStore {
    private val stateMap = ConcurrentHashMap<RoomId, ChatState>()
    private val stateChannels = ConcurrentHashMap<RoomId, Channel<ChatState>>()

    override fun getState(roomId: RoomId): ChatState {
        return stateMap[roomId] ?: ChatState()
    }

    override fun updateState(roomId: RoomId, newState: ChatState) {
        stateMap[roomId] = newState
        stateChannels[roomId]?.trySend(newState)
    }

    override fun createRoom(roomId: RoomId): ChatState {
        val initialState = ChatState()
        stateMap[roomId] = initialState
        stateChannels[roomId] = Channel(Channel.CONFLATED)
        return initialState
    }

    override fun deleteRoom(roomId: RoomId) {
        stateMap.remove(roomId)
        stateChannels.remove(roomId)?.close()
    }

    override fun listRooms(): Set<RoomId> {
        return stateMap.keys.toSet()
    }

    override fun observeState(roomId: RoomId): Flow<ChatState> = flow {
        val channel = stateChannels.getOrPut(roomId) { Channel(Channel.CONFLATED) }
        emit(getState(roomId))
        for (state in channel) {
            emit(state)
        }
    }
}