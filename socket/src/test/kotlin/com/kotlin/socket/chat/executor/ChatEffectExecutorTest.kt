package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.StringRedisTemplate

class ChatEffectExecutorTest : DescribeSpec({
    val mockBroadcastExecutor = mockk<BroadcastEffectExecutor>()
    val mockPersistMessageExecutor = mockk<PersistMessageEffectExecutor>()
    val mockUserStateEffectExecutor = mockk<UserStateEffectExecutor>()
    val mockRedisTemplate = mockk<StringRedisTemplate>()
    val executor = ChatEffectExecutor(mockBroadcastExecutor, mockPersistMessageExecutor, mockUserStateEffectExecutor, mockRedisTemplate)

    describe("ChatEffectExecutor") {
        describe("효과 실행") {
            it("Broadcast 효과를 실행할 수 있어야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello")
                coEvery { mockBroadcastExecutor.execute(any()) } returns Either.Right(Unit)

                // when
                val result = runBlocking { executor.runEffects(listOf(effect)).first() }

                // then
                result.isRight() shouldBe true
            }

            it("PersistMessage 효과를 실행할 수 있어야 한다") {
                // given
                val effect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                coEvery { mockPersistMessageExecutor.execute(any()) } returns Either.Right(Unit)

                // when
                val result = runBlocking { executor.runEffects(listOf(effect)).first() }

                // then
                result.isRight() shouldBe true
            }

            it("Log 효과를 실행할 수 있어야 한다") {
                // given
                val effect = ChatEffect.Log("Test log message")

                // when
                val result = runBlocking { executor.runEffects(listOf(effect)).first() }

                // then
                result.isRight() shouldBe true
            }

            it("여러 효과를 동시에 실행할 수 있어야 한다") {
                // given
                val effects = listOf(
                    ChatEffect.Broadcast("room1", "Hello"),
                    ChatEffect.PersistMessage("room1", 1L, "Hello"),
                    ChatEffect.Log("Test log message")
                )
                coEvery { mockBroadcastExecutor.execute(any()) } returns Either.Right(Unit)
                coEvery { mockPersistMessageExecutor.execute(any()) } returns Either.Right(Unit)

                // when
                val results = runBlocking { 
                    executor.runEffects(effects).toList() 
                }

                // then
                results.size shouldBe 3
                results.all { it.isRight() } shouldBe true
            }

            it("효과 실행 중 에러가 발생하면 Left를 반환해야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello")
                coEvery { mockBroadcastExecutor.execute(any()) } returns Either.Left(ChatError.InvalidMessage("Broadcast failed"))

                // when
                val result = runBlocking { executor.runEffects(listOf(effect)).first() }

                // then
                result.isLeft() shouldBe true
                val error = result.leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Broadcast failed"
            }
        }
    }
}) 