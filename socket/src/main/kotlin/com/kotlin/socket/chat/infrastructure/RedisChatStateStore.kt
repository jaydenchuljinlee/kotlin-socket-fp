package com.kotlin.socket.chat.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.model.RoomId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RedisChatStateStore(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : ChatStateStore {
    private val stateChannels = ConcurrentHashMap<RoomId, Channel<ChatState>>()
    private fun key(roomId: RoomId): String = "chat:state:${roomId.value}"

    override fun getState(roomId: RoomId): ChatState {
        val json = redisTemplate.opsForValue().get(key(roomId))
        return json?.let { objectMapper.readValue(it, ChatState::class.java) } ?: ChatState()
    }

    override fun updateState(roomId: RoomId, newState: ChatState) {
        val value = objectMapper.writeValueAsString(newState)
        redisTemplate.opsForValue().set(key(roomId), value)
        stateChannels[roomId]?.trySend(newState)
    }

    override fun createRoom(roomId: RoomId): ChatState {
        val initialState = ChatState()
        val value = objectMapper.writeValueAsString(initialState)
        redisTemplate.opsForValue().set(key(roomId), value)
        stateChannels[roomId] = Channel(Channel.CONFLATED)
        return initialState
    }

    override fun deleteRoom(roomId: RoomId) {
        redisTemplate.delete(key(roomId))
        stateChannels.remove(roomId)?.close()
    }

    override fun listRooms(): Set<RoomId> {
        // Redis에 저장된 모든 키 중 prefix가 chat:state: 인 것만 추출
        val keys = redisTemplate.keys("chat:state:*") ?: emptySet()
        return keys.mapNotNull {
            it.removePrefix("chat:state:").takeIf { id -> id.isNotBlank() }?.let(::RoomId)
        }.toSet()
    }

    override fun observeState(roomId: RoomId): Flow<ChatState> = flow {
        val channel = stateChannels.getOrPut(roomId) { Channel(Channel.CONFLATED) }
        emit(getState(roomId))
        for (state in channel) {
            emit(state)
        }
    }

}