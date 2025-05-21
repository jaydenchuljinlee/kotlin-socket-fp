# 💡 채팅 서버 개선 학습 정리

## ✅ 개선 사항 요약

### 1. 유저 상태 관리 기능 추가
- 각 채팅방(`roomId`)의 유저 접속 상태(`Join`, `Leave`)를 별도로 추적
- 효과(`ChatEffect`)로 분리: `JoinUser`, `LeaveUser` 추가
- 유저가 채팅방에 들어오거나 나갈 때 `ChatState` 외에도 별도의 저장소에 상태를 반영

### 2. 메시지 캐싱 기능 추가
- Redis를 이용해 최근 메시지를 캐시
- 채팅방별로 최근 N개의 메시지를 유지 (`LPUSH` + `LTRIM`)
- 효과(`ChatEffect`)로 `CacheMessage` 추가

---

## 📌 설계 및 구현 내용

### ✨ ChatEffect 확장

```kotlin
sealed class ChatEffect {
    data class Broadcast(val roomId: String, val message: String) : ChatEffect()
    data class PersistMessage(val roomId: String, val from: Long, val content: String) : ChatEffect()
    data class Log(val content: String) : ChatEffect()
    data class JoinUser(val roomId: String, val userId: Long) : ChatEffect()
    data class LeaveUser(val roomId: String, val userId: Long) : ChatEffect()
    data class CacheMessage(val roomId: String, val message: String) : ChatEffect()
}
```

### ✨ 실행기 (Executor) 구조

```kotlin
@Component
class ChatEffectExecutor(...) {
    suspend fun runEffects(effects: List<ChatEffect>): Flow<Either<ChatError, Unit>> = flow {
        for (effect in effects) {
            val result = try {
                when (effect) {
                    is Broadcast      -> broadcastExecutor.execute(effect)
                    is PersistMessage -> persistMessageExecutor.execute(effect)
                    is JoinUser, 
                    is LeaveUser      -> userStateEffectExecutor.execute(effect)
                    is CacheMessage   -> cacheToRedis(effect)
                    is Log            -> logEffect(effect)
                }
            } catch (e: Exception) {
                Either.Left(ChatError.InvalidMessage("Effect failure: ${e.message}"))
            }
            emit(result)
        }
    }
}
```

## 🧨 주요 이슈 및 해결 과정

### 🐞 이슈 1. PersistMessage가 실행되지 않음
- 
- 원인: executor.runEffects(effects) 호출 후 .collect()를 하지 않아 Flow가 구동되지 않음
- 해결: .collect {} 또는 .collect() 명시적으로 호출하여 모든 효과 실행

```kotlin
executor.runEffects(effects).collect()
```

### 🐞 이슈 2. PersistMessageEffectExecutor 로그 미출력

- 원인: 효과가 생성되지 않은 게 아니라 실행되지 않아서 로그가 찍히지 않음
- 해결: ChatInterpreter에서 생성된 효과 목록을 로그로 출력해 확인 후 .collect() 적용

### 🐞 이슈 3. Redis 캐싱은 실행되지만 효과가 확인되지 않음

- 원인: CacheMessage는 try-catch 내부에서 예외를 무시했기 때문에 실패 로그도 없었음
- 해결: cacheToRedis 함수 내부에 logger.info 로그 명시

## 🧠 학습 포인트

| 항목 | 설명 |
|------|------|
| 🔄 Flow는 Lazy하다 | Kotlin의 `Flow`는 `collect()`가 호출되기 전까지 실행되지 않음. `runEffects()`를 호출했더라도 `collect()`하지 않으면 효과가 실행되지 않음 |
| 📦 효과 시스템 분리 | `ChatEffect`로 비즈니스 후처리를 선언하고, `ChatEffectExecutor`에서 실행을 위임하는 구조는 관심사의 분리를 극대화함 |
| 🧪 순수 해석 + 부수 효과 실행 | `ChatInterpreter`는 `Effect`를 리턴만 하고 실제 실행하지 않음 → 테스트 용이성과 예측 가능성 향상 |
| ⚠️ 예외 처리 중요성 | Redis 캐시나 외부 저장소 연동 시 예외가 발생해도 로그가 없으면 원인을 찾기 어려움. **효과 내부에서 예외 처리는 반드시 로깅 포함** 필요 |
| 🔒 락과 상태 저장 시점 | 상태 저장(`updateState`) 전에 락을 걸어야 하며, 효과 실행은 락 안에서 일관되게 처리하는 것이 안전함 |

---

## ✅ 정리

- `Broadcast` 외의 효과(`PersistMessage`, `JoinUser`, `CacheMessage`)는 모두 **`runEffects().collect()`로 소비되어야 실행됨**
- `Flow` 기반 구조에서는 반드시 `.collect()`나 `.toList()` 등 소비 연산자를 붙여야 효과가 실제로 발생
- 효과 기반 분리는 유지보수성과 확장성에 유리하며, 실제 실행 책임과 선언 책임을 분리함으로써 테스트 및 추적이 쉬워짐
- Redis 캐시, DB 저장, 사용자 상태 저장은 모두 **Effect 단위**로 분리하여 **로그와 예외처리**를 각각 다뤄야 함

---

