package com.kotlin.socket.chat.handler

import com.kotlin.socket.chat.dto.ChatState
import com.kotlin.socket.chat.error.ChatError
import com.kotlin.socket.chat.model.ChatCommand
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ChatInterpreterTest : DescribeSpec({
    describe("ChatInterpreter") {
        describe("메시지 전송") {
            it("빈 메시지를 보내면 InvalidMessage 에러를 반환해야 한다") {
                // given
                val cmd = ChatCommand.SendMessage("room1", 1L, 2L, "")
                val state = ChatState(activeUsers = setOf(1L, 2L))

                // when
                val result = ChatInterpreter.interpret(cmd, state)

                // then
                result.isLeft() shouldBe true
                val error = result.leftOrNull()
                error.shouldBeInstanceOf<ChatError.InvalidMessage>()
                (error as ChatError.InvalidMessage).reason shouldBe "Message is blank"
            }
        }

        describe("채팅방 퇴장") {
            it("존재하지 않는 사용자가 채팅방을 나가려고 하면 UserNotFound 에러를 반환해야 한다") {
                // given
                val cmd = ChatCommand.Leave("room1", 999L)
                val state = ChatState(activeUsers = setOf(1L, 2L))

                // when
                val result = ChatInterpreter.interpret(cmd, state)

                // then
                result.isLeft() shouldBe true
                val error = result.leftOrNull()
                error.shouldBeInstanceOf<ChatError.UserNotFound>()
                (error as ChatError.UserNotFound).userId shouldBe 999L
            }
        }

        describe("채팅방 입장") {
            it("이미 채팅방에 있는 사용자가 다시 입장하려고 하면 UserAlreadyInRoom 에러를 반환해야 한다") {
                // given
                val cmd = ChatCommand.Join("room1", 1L)
                val state = ChatState(activeUsers = setOf(1L))

                // when
                val result = ChatInterpreter.interpret(cmd, state)

                // then
                result.isLeft() shouldBe true
                val error = result.leftOrNull()
                error.shouldBeInstanceOf<ChatError.UserAlreadyInRoom>()
                (error as ChatError.UserAlreadyInRoom).userId shouldBe 1L
            }
        }
    }
}) 