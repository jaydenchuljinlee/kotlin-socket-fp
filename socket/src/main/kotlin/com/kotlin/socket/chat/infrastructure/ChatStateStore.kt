package com.kotlin.socket.chat.infrastructure

import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.model.RoomId
import kotlinx.coroutines.flow.Flow

interface ChatStateStore {
    fun getState(roomId: RoomId): ChatState
    fun updateState(roomId: RoomId, newState: ChatState)
    
    // 새로운 채팅방 관련 메서드들
    fun createRoom(roomId: RoomId): ChatState
    fun deleteRoom(roomId: RoomId)
    fun listRooms(): Set<RoomId>
    
    // 상태 변경 이벤트를 위한 Flow
    fun observeState(roomId: RoomId): Flow<ChatState>
}