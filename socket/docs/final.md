# 🧠 함수형 프로그래밍 적용 정리

## ✅ 함수형 프로그래밍이란?

함수형 프로그래밍(Functional Programming, FP)은 프로그램을 **순수 함수(pure function)**와 **불변성(immutability)** 중심으로 구성하는 프로그래밍 패러다임입니다.  
FP는 다음과 같은 원칙에 기반합니다:

| 원칙 | 설명 |
|------|------|
| **순수 함수** | 동일한 입력에 대해 항상 동일한 출력을 반환하며, 외부 상태를 변경하지 않음 |
| **불변성** | 상태를 직접 변경하지 않고 새로운 값을 생성함 |
| **부작용 없음(Side-effect free)** | I/O나 DB 작업 등 외부에 영향을 주는 작업은 분리 |
| **고차 함수(Higher-order functions)** | 함수를 인자로 넘기거나 함수로 반환할 수 있음 |
| **선언형 스타일** | 명령형보다 선언형으로 "무엇"을 할지 표현 (`map`, `fold`, `flatMap` 등) |

---

## 🧩 현재 프로젝트에서 FP를 어떻게 적용했는가?

| 구조 | 설명 |
|------|------|
| `ChatCommand` | 명령을 표현하는 불변 데이터 |
| `ChatEffect` | 부작용(전송, 저장, 캐시 등)을 표현하는 불변 데이터 |
| `ChatInterpreter` | 상태 + 명령을 받아 부작용을 반환하는 순수 함수 역할 |
| `ChatEffectExecutor` | 실제 부작용을 실행하는 레이어로, 명령과 분리 |
| `Either<ChatError, T>` | 오류 처리 시 명확한 분기와 안전한 컴포지션 제공 |
| `runEffects(): Flow<Either<...>>` | 부작용 실행을 명시적으로 표현하고, 비동기 처리에 적합 |

---

## 🤔 FP스럽지 않아도 됐던 부분

| 부분 | 설명 |
|------|------|
| `object` 형태의 핸들러 | 단순한 호출을 위해서는 `@Component` 기반의 클래스여도 충분함 |
| Interpreter ↔ Effect ↔ Executor 분리 | 간단한 로직일 경우 오히려 구조가 무거워지고 진입장벽이 커질 수 있음 |
| Command / Effect 계층 남용 | 작은 프로젝트에서는 레이어가 지나치게 세분화될 수 있음 |

→ 하지만 **학습과 확장성** 면에서는 가치가 있었음.

---

## 🌟 잘 적용된 FP 원칙과 코드 예시

### 🔒 불변성

```kotlin
val updated = current.copy(activeUsers = current.activeUsers + userId)
```

### 🧼 순수 함수: ChatInterpreter.interpret
```kotlin
when (cmd) {
    is ChatCommand.SendMessage -> {
        if (cmd.content.isBlank()) {
            ChatError.InvalidMessage("Message is blank").left()
        } else {
            val message = "${cmd.from}: ${cmd.content}"
            (state to listOf(
                ChatEffect.Broadcast(cmd.roomId, message),
                ChatEffect.PersistMessage(cmd.roomId, cmd.from, cmd.content),
                ChatEffect.CacheMessage(cmd.roomId, message),
                ChatEffect.Log("Message from ${cmd.from} in ${cmd.roomId}")
            )).right()
        }
    }
}

```

- 외부 의존 없이, 입력값만으로 결과를 도출
- Either로 오류와 정상 결과를 명확하게 분리

---

### 🚫 Side Effect 분리
- ChatEffectExecutor가 실제 부작용을 실행하는 역할을 맡음
- 메시지 전송, 저장, 캐싱, 로그 등은 모두 분리된 executor로 위임

---

### 🔄 고차 함수 활용
```kotlin
effects.forEach { effect ->
    val result = when (effect) {
        is ChatEffect.Broadcast -> broadcastExecutor.execute(effect)
        ...
    }
    emit(result)
}
```
- Executor 내부에서 함수를 조합하고 위임

---

### 📌 결론 (함수형 프로그래밍을 실무에 어떻게 적용할 것인가?)

- Entity 객체에 대한 불변성 확보
- Either를 활용하여 Side-Effect-Free 하게 개발
- Stream API를 활용하여 선언형 스타일 적용
- Functional Interface를 통한 고차 함수 활용
- 도메인 객체 내에 순수 함수 도입을 통해 SRP 및 외부 상태를 변경하지 않도록 한다. 


