package com.kotlin.socket.chat

import com.kotlin.socket.chat.infrastructure.inmemory.store.RedisChatStateStore
import com.kotlin.socket.chat.model.RoomId
import com.kotlin.socket.common.lock.RedissonLockSupporter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExperimentalCoroutinesApi
@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class ChatServiceIntegrationTest(
    @Autowired val redisChatStateStore: RedisChatStateStore,
    @Autowired val lockSupporter: RedissonLockSupporter
): DescribeSpec() {
    init {
        val roomId = RoomId("concurrent-room")

        beforeTest {
            redisChatStateStore.deleteRoom(roomId)
            redisChatStateStore.createRoom(roomId)
        }

        afterTest {
            redisChatStateStore.deleteRoom(roomId)
        }

        describe("RedissonLockSupporter") {

            describe("동시 join 요청 처리") {

                it("10명의 유저가 동시에 입장해도 충돌 없이 상태가 갱신되어야 한다") {
                    // given
                    val joinCount = 10
                    val userIds = (1L..joinCount).toList()

                    // when
                    runBlocking {
                        coroutineScope {
                            userIds.map { userId ->
                                launch {
                                    lockSupporter.withLockSuspend("lock:room:${roomId.value}") {
                                        val current = redisChatStateStore.getState(roomId)
                                        val updated = current.copy(activeUsers = current.activeUsers + userId)
                                        redisChatStateStore.updateState(roomId, updated)
                                    }
                                }
                            }.joinAll()
                        }
                    }

                    // then
                    val finalState = redisChatStateStore.getState(roomId)

                    finalState.activeUsers.size shouldBe joinCount
                    finalState.activeUsers shouldContainAll userIds
                }
            }
        }
    }
}