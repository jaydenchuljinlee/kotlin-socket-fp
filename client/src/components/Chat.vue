<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 py-8 px-4">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-6">
        <h1 class="text-3xl font-bold text-gray-800 mb-2">실시간 채팅</h1>
        <p class="text-gray-600">WebSocket을 이용한 실시간 메시지 교환</p>
      </div>

      <!-- Main Chat Container -->
      <div class="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
        <!-- Connection Section -->
        <div class="p-6 bg-gradient-to-r from-blue-500 to-indigo-600 border-b border-gray-200">
          <div class="flex flex-col sm:flex-row sm:items-end gap-4">
            <div class="flex-1">
              <label class="block text-sm font-medium text-white mb-2">사용자 ID</label>
              <input 
                v-model="userId" 
                type="text" 
                class="w-full h-11 px-4 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-white/50"
                placeholder="사용자 ID를 입력하세요"
              />
            </div>
            <div class="flex-1">
                <label class="block text-sm font-medium text-white mb-2">채팅방 ID</label>
                <input 
                    v-model="roomId" 
                    type="text" 
                    class="w-full h-11 px-4 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-white/50"
                    placeholder="roomId를 입력하세요"
                />
            </div>
            <button 
              v-if="!connected" 
              @click="connect" 
              class="w-full sm:w-auto h-11 px-6 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-white/50 font-medium shadow-lg flex items-center justify-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd" />
              </svg>
              <span>연결하기</span>
            </button>
            <button 
              v-else 
              @click="disconnect" 
              class="w-full sm:w-auto h-11 px-6 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-500/50 font-medium shadow-lg flex items-center justify-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd" />
              </svg>
              <span>연결 해제</span>
            </button>
          </div>
        </div>

        <!-- Chat Section -->
        <div v-if="connected" class="flex flex-col h-[700px]">
          <!-- Status Bar -->
          <div class="flex items-center justify-between p-4 bg-gray-50 border-b border-gray-200">
            <div class="flex items-center space-x-2">
              <div class="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
              <span class="text-sm text-gray-600">연결됨: <strong class="text-gray-900">{{ userId }}</strong></span>
            </div>
            <div class="text-xs text-gray-500">
              {{ new Date().toLocaleTimeString() }}
            </div>
          </div>

          <!-- Messages -->
          <div class="flex-1 overflow-auto p-6 bg-gray-50 space-y-4" ref="messageContainer">
            <div 
              v-for="(msg, index) in messages" 
              :key="index" 
              :class="[
                'max-w-[80%] break-words',
                isMyMessage(msg) ? 'ml-auto' : 'mr-auto'
              ]"
            >
              <div class="flex items-end gap-2">
                <div v-if="!isMyMessage(msg)" class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center">
                    <span class="text-sm text-gray-600">{{ getInitials(msg) }}</span>
                  </div>
                </div>
                <div
                  :class="[
                    'px-4 py-2 rounded-2xl shadow-sm',
                    isMyMessage(msg) 
                      ? 'bg-blue-500 text-white rounded-br-none' 
                      : 'bg-white text-gray-800 rounded-bl-none border border-gray-200'
                  ]"
                >
                  <div class="text-xs mb-1" :class="isMyMessage(msg) ? 'text-blue-100' : 'text-gray-500'">
                    {{ getSender(msg) }}
                  </div>
                  <div class="break-words">{{ getMessageContent(msg) }}</div>
                </div>
                <div v-if="isMyMessage(msg)" class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center">
                    <span class="text-sm text-white">{{ getInitials(msg) }}</span>
                  </div>
                </div>
              </div>
              <div 
                class="text-xs mt-1 text-gray-400"
                :class="isMyMessage(msg) ? 'text-right' : 'text-left'"
              >
                {{ getCurrentTime() }}
              </div>
            </div>
          </div>

          <!-- Message Input -->
          <div class="p-4 bg-white border-t border-gray-200">
            <div class="flex flex-col sm:flex-row gap-3">
              <div class="w-full sm:w-1/4">
                <input 
                  v-model="to" 
                  placeholder="받는 사람" 
                  class="w-full h-11 px-4 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50 shadow-sm"
                />
              </div>
              <div class="flex-1 flex gap-2">
                <input 
                  v-model="content" 
                  placeholder="메시지를 입력하세요..." 
                  class="flex-1 h-11 px-4 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50 shadow-sm"
                  @keyup.enter="sendMessage"
                />
                <button 
                  @click="sendMessage" 
                  class="h-11 px-6 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 font-medium flex items-center justify-center gap-2 shadow-lg whitespace-nowrap"
                >
                  <span>전송</span>
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Disconnected State -->
        <div v-else class="p-12 text-center text-gray-500">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto mb-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <p class="text-lg">채팅을 시작하려면 사용자 ID를 입력하고 연결해주세요.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const stompClient = ref(null)
const userId = ref('')
const roomId = ref('')
const to = ref('')
const content = ref('')
const messages = ref([])
const connected = ref(false)
const messageContainer = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

watch(messages, scrollToBottom)

const connect = () => {
  if (!userId.value || !roomId.value) return alert('사용자 ID와 Room ID를 입력해주세요')

  const socket = new SockJS('http://localhost:8080/ws')
  stompClient.value = new Client({
    webSocketFactory: () => socket,
    onConnect: () => {
      connected.value = true
      stompClient.value.subscribe(`/topic/chatroom/${roomId.value}`, (message) => {
        try {
          const parsed = JSON.parse(message.body)
          console.log(parsed);
          messages.value.push(parsed)
        } catch {
          messages.value.push({ content: message.body })
          console.log(messages.value);
        }
      })

      stompClient.value.publish({
        destination: `/app/chat/join/${roomId.value}`,
        body: JSON.stringify({ userId: Number(userId.value) })
      })
    },
    onDisconnect: () => {
      connected.value = false
      messages.value.push({ content: '[시스템] 연결이 해제되었습니다.' })
    }
  })

  stompClient.value.activate()
}

const disconnect = () => {
  if (stompClient.value) {
    stompClient.value.publish({
      destination: `/app/chat/leave/${roomId.value}`,
      body: JSON.stringify({ userId: Number(userId.value) })
    })
    stompClient.value.deactivate()
    stompClient.value = null
    connected.value = false
    messages.value = []
  }
}

const sendMessage = () => {
  if (!to.value || !content.value) return
  stompClient.value.publish({
    destination: `/app/chat/message/${roomId.value}`,
    body: JSON.stringify({
      from: Number(userId.value),
      to: Number(to.value),
      content: content.value,
    })
  })
  content.value = ''
}

const isMyMessage = (msg) => msg.from === Number(userId.value)
const getMessageContent = (msg) => msg.content || msg
const getSender = (msg) => msg.from || '시스템'
const getInitials = (msg) => String(getSender(msg)).substring(0, 2).toUpperCase()
const getCurrentTime = () => new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
</script>

<style scoped>
/* Custom scrollbar */
.overflow-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.overflow-auto::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* Smooth transitions */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

/* Gradient animation */
@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.bg-gradient-to-r {
  background-size: 200% 200%;
  animation: gradient 15s ease infinite;
}

/* Message text wrap */
.break-words {
  word-break: break-word;
  overflow-wrap: break-word;
}
</style>

