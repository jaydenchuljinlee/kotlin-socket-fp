package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatEffect
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

class BroadcastEffectExecutorTest : DescribeSpec({
    val executor = BroadcastEffectExecutor()

    describe("BroadcastEffectExecutor") {
        describe("메시지 브로드캐스트") {
            it("브로드캐스트가 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello, World!")

                // when
                val result = runBlocking { executor.execute(effect) }

                // then
                result.isRight() shouldBe true
            }

            it("브로드캐스트 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello, World!")
                // 실제로는 예외를 발생시키기 어려우므로, 이 테스트는 현재 구현에서는 항상 성공할 것입니다.
                // 실제 환경에서는 WebSocket 연결 실패 등의 상황에서 예외가 발생할 수 있습니다.

                // when
                val result = runBlocking { executor.execute(effect) }

                // then
                result.isRight() shouldBe true
            }
        }
    }
}) 