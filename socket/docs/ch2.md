## ✅ 프로젝트 구조 개선 요약: AS-IS vs TO-BE

### 1. 아키텍처 개요

| 항목 | AS-IS (기존 구조) | TO-BE (함수형 구조 + 확장성 고려) |
|------|-------------------|-----------------------------------|
| 상태 관리 방식 | Controller 내부 전역 변수 (`var state`) | `ChatStateStore`를 통한 외부 상태 위임 (채팅방 단위 분리) |
| 메시지 처리 방식 | Controller가 직접 메시지 가공 및 응답 | `ChatCommand` → `ChatInterpreter` → `ChatEffect` 로 의도와 실행 분리 |
| Side Effect 처리 | Controller 내부에서 println, DB 저장 직접 수행 | `ChatEffectExecutor`에서 명시적 실행 (suspend 기반 실행기) |
| 테스트 용이성 | 상태/로직/출력이 혼재되어 테스트 어려움 | 상태와 로직, 효과 분리로 단위 테스트 가능 |
| 확장성 | 로직마다 직접 코드 삽입 필요 | 명령·효과 기반 구조로 손쉬운 기능 확장 |

---

### 2. 핵심 클래스 비교

| 클래스 | AS-IS | TO-BE |
|--------|--------|--------|
| `ChatController` | 상태와 로직, 부작용을 모두 포함 | 순수 위임 역할만 수행 (thin controller) |
| `ChatState` | 단일 상태 객체, 전역 관리 | 채팅방별 상태 분리 (`Map<roomId, ChatState>`) |
| `ChatCommand` | 없음 | 사용자의 의도를 명시적으로 표현하는 명령 객체 |
| `ChatEffect` | 없음 | 부작용(Broadcast, 저장 등)을 표현하는 효과 객체 |
| `ChatInterpreter` | 없음 | 명령을 해석해 상태 변화와 효과를 반환하는 순수 함수 |
| `ChatEffectExecutor` | 없음 | 효과를 실제 실행하는 suspend 기반 효과 실행기 |

---

### 3. 확장 가능한 TO-BE 구조: 다음 단계 설계

#### 🛰️ 1. 외부 서비스 연동 (FCM, Kafka 등)
- `ChatEffect`에 새로운 타입 추가:
  ```kotlin
  data class PushNotification(val userId: Long, val message: String) : ChatEffect
  data class SendToKafka(val topic: String, val payload: String) : ChatEffect```
- ChatEffectExecutor에서 해당 분기 처리
- Side Effect는 구조적으로 통합 관리됨

#### 2. 에러 처리 분리 (Either, Validated)
- ChatInterpreter.interpret 반환 타입을 다음처럼 변경:
  ```kotlin
 fun interpret(cmd: ChatCommand, state: ChatState): Either<ChatError, Pair<ChatState, List<ChatEffect>>>```
- 잘못된 명령, 인증 실패, 제한 메시지 등을 명시적으로 처리 가능

#### 3. 상태 분산 (Redis or Sharded Room)
- ChatStateStore 구현체를 RedisChatStateStore로 교체
- roomId 기반 분산 저장 가능
- 확장 시에도 구조 변경 없이 Bean 교체만으로 대응

