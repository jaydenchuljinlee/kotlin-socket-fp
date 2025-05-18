package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.infrastructure.ChatHistoryStore
import com.kotlin.socket.chat.model.ChatEffect
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class PersistMessageEffectExecutorTest : DescribeSpec({
    val mockHistoryStore = mockk<ChatHistoryStore>()
    val executor = PersistMessageEffectExecutor(mockHistoryStore)

    describe("PersistMessageEffectExecutor") {
        describe("메시지 저장") {
            it("메시지 저장이 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                coEvery { mockHistoryStore.saveMessage(any(), any(), any()) } returns Unit

                // when
                val result = runBlocking { executor.execute(effect) }

                // then
                result.isRight() shouldBe true
                coVerify { mockHistoryStore.saveMessage("room1", 1L, "Hello") }
            }

            it("메시지 저장 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                coEvery { mockHistoryStore.saveMessage(any(), any(), any()) } throws RuntimeException("저장 실패")

                // when
                val result = runBlocking { executor.execute(effect) }

                // then
                result.isLeft() shouldBe true
                val error = result.leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Failed to persist message: 저장 실패"
            }
        }
    }
}) 