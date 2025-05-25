package com.kotlin.socket.chat.executor

import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatEffect
import arrow.core.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.StringRedisTemplate

class PersistMessageEffectExecutorTest : DescribeSpec({
    val mockBroadcastExecutor = mockk<BroadcastEffectExecutor>()
    val mockPersistMessageExecutor = mockk<PersistMessageEffectExecutor>()
    val mockUserStateEffectExecutor = mockk<UserStateEffectExecutor>()
    val mockRedisTemplate = mockk<StringRedisTemplate>()
    val mockListOps = mockk<ListOperations<String, String>>()

    val executor = ChatEffectExecutor(
        broadcastExecutor = mockBroadcastExecutor,
        persistMessageExecutor = mockPersistMessageExecutor,
        userStateEffectExecutor = mockUserStateEffectExecutor,
        redisTemplate = mockRedisTemplate
    )

    beforeEach {
        every { mockRedisTemplate.opsForList() } returns mockListOps
    }

    describe("ChatEffectExecutor") {
        describe("Broadcast Effect 처리") {
            it("Broadcast 효과가 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello")
                val effects = listOf(effect)
                coEvery { mockBroadcastExecutor.execute(effect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
                coVerify { mockBroadcastExecutor.execute(effect) }
            }

            it("Broadcast 실행 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.Broadcast("room1", "Hello")
                val effects = listOf(effect)
                coEvery { mockBroadcastExecutor.execute(effect) } throws RuntimeException("브로드캐스트 실패")

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isLeft() shouldBe true
                val error = results[0].leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Effect failure: 브로드캐스트 실패"
            }
        }

        describe("PersistMessage Effect 처리") {
            it("메시지 저장이 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                val effects = listOf(effect)
                coEvery { mockPersistMessageExecutor.execute(effect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
                coVerify { mockPersistMessageExecutor.execute(effect) }
            }

            it("메시지 저장 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                val effects = listOf(effect)
                coEvery { mockPersistMessageExecutor.execute(effect) } throws RuntimeException("저장 실패")

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isLeft() shouldBe true
                val error = results[0].leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Effect failure: 저장 실패"
            }
        }

        describe("JoinUser Effect 처리") {
            it("사용자 참가가 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.JoinUser("room1", 1L)
                val effects = listOf(effect)
                coEvery { mockUserStateEffectExecutor.execute(effect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
                coVerify { mockUserStateEffectExecutor.execute(effect) }
            }

            it("사용자 참가 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.JoinUser("room1", 1L)
                val effects = listOf(effect)
                coEvery { mockUserStateEffectExecutor.execute(effect) } throws RuntimeException("참가 실패")

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isLeft() shouldBe true
                val error = results[0].leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Effect failure: 참가 실패"
            }
        }

        describe("LeaveUser Effect 처리") {
            it("사용자 퇴장이 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.LeaveUser("room1", 1L)
                val effects = listOf(effect)
                coEvery { mockUserStateEffectExecutor.execute(effect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
                coVerify { mockUserStateEffectExecutor.execute(effect) }
            }

            it("사용자 퇴장 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.LeaveUser("room1", 1L)
                val effects = listOf(effect)
                coEvery { mockUserStateEffectExecutor.execute(effect) } throws RuntimeException("퇴장 실패")

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isLeft() shouldBe true
                val error = results[0].leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Effect failure: 퇴장 실패"
            }
        }

        describe("CacheMessage Effect 처리") {
            it("메시지 캐싱이 성공하면 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.CacheMessage("room1", "Hello World")
                val effects = listOf(effect)
                every { mockListOps.leftPush(any<String>(), any<String>()) } returns 1L
                every { mockListOps.trim(any(), any(), any()) } returns Unit

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
                verify { mockListOps.leftPush("room:room1:messages", "Hello World") }
                verify { mockListOps.trim("room:room1:messages", 0, 99) }
            }

            it("Redis 캐싱 중 예외가 발생하면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val effect = ChatEffect.CacheMessage("room1", "Hello World")
                val effects = listOf(effect)
                every { mockListOps.leftPush(any<String>(), any<String>()) } throws RuntimeException("Redis 연결 실패")

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isLeft() shouldBe true
                val error = results[0].leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Redis Cache Error: Redis 연결 실패"
            }
        }

        describe("Log Effect 처리") {
            it("로그 효과는 항상 Right(Unit)를 반환해야 한다") {
                // given
                val effect = ChatEffect.Log("테스트 로그 메시지")
                val effects = listOf(effect)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 1
                results[0].isRight() shouldBe true
            }
        }

        describe("다중 Effect 처리") {
            it("여러 Effect를 순차적으로 처리하고 모든 결과를 반환해야 한다") {
                // given
                val broadcastEffect = ChatEffect.Broadcast("room1", "Hello")
                val persistEffect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                val logEffect = ChatEffect.Log("테스트 로그")
                val effects = listOf(broadcastEffect, persistEffect, logEffect)

                coEvery { mockBroadcastExecutor.execute(broadcastEffect) } returns Either.Right(Unit)
                coEvery { mockPersistMessageExecutor.execute(persistEffect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 3
                results.all { it.isRight() } shouldBe true
                coVerify { mockBroadcastExecutor.execute(broadcastEffect) }
                coVerify { mockPersistMessageExecutor.execute(persistEffect) }
            }

            it("일부 Effect가 실패해도 나머지 Effect는 계속 처리되어야 한다") {
                // given
                val broadcastEffect = ChatEffect.Broadcast("room1", "Hello")
                val persistEffect = ChatEffect.PersistMessage("room1", 1L, "Hello")
                val logEffect = ChatEffect.Log("테스트 로그")
                val effects = listOf(broadcastEffect, persistEffect, logEffect)

                coEvery { mockBroadcastExecutor.execute(broadcastEffect) } throws RuntimeException("브로드캐스트 실패")
                coEvery { mockPersistMessageExecutor.execute(persistEffect) } returns Either.Right(Unit)

                // when
                val results = runBlocking { executor.runEffects(effects).toList() }

                // then
                results.size shouldBe 3
                results[0].isLeft() shouldBe true // 실패
                results[1].isRight() shouldBe true // 성공
                results[2].isRight() shouldBe true // 성공 (로그는 항상 성공)
            }
        }
    }
})