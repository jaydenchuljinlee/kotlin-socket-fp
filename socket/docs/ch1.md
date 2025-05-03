# 🧠 Functional WebSocket Chat – AS-IS vs TO-BE 정리

## ✅ AS-IS: 현재 구조 (부분적인 함수형 스타일 적용)

| 항목                          | 설명 |
|-------------------------------|------|
| `ChatEvent`                   | `sealed class`로 모델링되어 타입 안전한 이벤트 처리 가능 |
| `ChatEventHandler.handle()`   | 순수 함수로 입력(이벤트, 상태)에 따라 항상 동일한 출력 반환 |
| `ChatState`                   | 불변 객체로 상태를 `copy()`로 갱신 |
| `ChatController`              | 얇은 계층으로 handler에 위임하여 SRP 준수 |
| 외부 I/O                      | 현재 없음 (Message 저장 등 사이드 이펙트 없음) |
| 테스트 용이성                | handler 단위 테스트 가능함 |

---

## 🧩 TO-BE: 개선 방향 (함수형 설계 원칙을 더 반영)

| 개선 항목                           | 설명 |
|------------------------------------|------|
| 🔄 전역 `var state` 제거            | 상태를 함수형 구조로 위임하거나 외부 저장소/Context로 분리 (ex. 상태 주입 구조) |
| 🔌 사이드 이펙트 분리               | 메시지 저장, 브로커 발행 등의 I/O를 handler에서 분리하고 명시적으로 관리 (`Effect`, `IO`) |
| 🧰 상태 흐름 명시화                 | `ChatHandler` 결과를 `StateTransition`이나 `Command`로 표현 가능 |
| 🔀 비동기 효과 추상화               | 코루틴 또는 `Arrow.fx`의 `IO`, `Either` 등으로 부작용 제어 |
| 🧵 스레드 안전 상태 처리            | 상태 변경을 액터 모델 또는 `ConcurrentMap<roomId, ChatState>`로 관리 |
| 📦 테스트 가능한 메시지 파이프라인 | 이벤트 기반으로 구성하여 전체 흐름 단위 테스트 가능하게 구성 |

---

## 📌 개선 예시 아이디어

### 🔹 상태 주입형 구조 예

```kotlin
fun handle(event: ChatEvent): (ChatState) -> Pair<ChatState, MessageResponse>
