# 📘 WebSocket 기반 채팅 서버: Redis 상태 이관 & 부하 테스트 정리

---

## ✅ 1. Redis 기반 ChatStateStore 이관

### 🔍 기존 구조 (AS-IS)

- `InMemoryChatStateStore`에서 JVM 내에서 `ConcurrentHashMap`으로 상태 저장
- 단점:
    - 서버 재시작 시 상태 손실
    - 다중 인스턴스 환경에서 상태 공유 불가
    - 운영/테스트 환경에서 확장 어려움

### ✅ 개선 구조 (TO-BE)

- Redis 기반 상태 저장소 `RedisChatStateStore` 구현
- 주요 기능:
    - `getState(roomId)`: Redis 조회
    - `updateState(roomId, state)`: Redis 갱신
    - `observeState(roomId)`: `Channel` + `Pub/Sub` 방식 구현
    - 방 생성/삭제/조회 지원

### 💡 이점

| 항목        | 설명 |
|-------------|------|
| 영속성       | 서버 재시작에도 상태 유지 가능 |
| 수평 확장    | 여러 서버 간 상태 공유 |
| 모니터링     | Redis key TTL, memory usage 등 사용 가능 |
| 확장성       | Kafka 등 이벤트 시스템과 연계 가능 |

---

## ✅ 2. WebSocket 부하 테스트 이슈 및 해결

### ❌ 문제: SockJS 환경에서는 성능 테스트 도구 사용 불가

#### 원인

- `withSockJS()`는 브라우저 호환성을 위한 **전용 transport 계층**을 포함
- STOMP 메시징도 텍스트 프레임 기반의 **특수한 프로토콜**
- `k6`, `ws`, `websocket-client`, `JMeter` 등은 **SockJS handshake 미지원**

#### 결과

- `WebSocket 연결 실패`, `프레임 drop`, `서버 미도달` 현상 발생

---

### ✅ 해결: SockJS 제거 후 표준 WebSocket 전환

#### Spring Boot 설정 변경

```kotlin
override fun registerStompEndpoints(registry: StompEndpointRegistry) {
    registry.addEndpoint("/ws") // ❌ withSockJS() 제거
        .setAllowedOriginPatterns("*")
}
```

#### Vue.js 클라이언트 변경
```javascript
// 변경 전
const socket = new SockJS('http://localhost:8080/ws')

// 변경 후
const socket = new WebSocket('ws://localhost:8080/ws')

```

## ✅ 3. 일반 WebSocket vs SockJS 비교

| 항목                        | WebSocket                              | SockJS                                       |
|-----------------------------|-----------------------------------------|----------------------------------------------|
| **프로토콜 표준성**        | ✅ RFC 6455 표준 기반                   | ❌ 비표준 프로토콜 (fallback 포함)            |
| **브라우저 호환성**        | 최신 브라우저만 지원                   | ✅ 구형 브라우저까지 지원                    |
| **Spring 설정 방식**       | `addEndpoint("/ws")`                   | `addEndpoint("/ws").withSockJS()`            |
| **연결 방식**              | WebSocket 직접 연결 (`ws://`)         | HTTP long-polling, xhr-streaming, iframe 등 |
| **테스트 도구 호환성**     | ✅ k6, ws, JMeter, Gatling 등 모두 지원 | ❌ 거의 모든 부하 테스트 도구와 미호환       |
| **메시징 단순성**          | STOMP 메시지만 다루면 됨              | SockJS 프로토콜 + STOMP 둘 다 고려 필요     |
| **운영 환경 확장성**       | ✅ 서버 간 분산, CDN 구성 용이         | ❌ 프록시/로드밸런서 설정이 복잡             |
| **성능 및 유지보수**       | 빠르고 유지보수 쉬움                   | 복잡하고 디버깅 어려움                        |
| **실무 사용**              | ✅ 게임, 챗, 실시간 대시보드 등        | ❌ 레거시 IE 대응 외에는 거의 사용 안 함     |

---

## 📌 요약

- `SockJS`는 구형 브라우저 대응에는 유리하지만, **성능 테스트/확장성/호환성 측면에서 불리**합니다.
- 특히 **k6 같은 성능 측정 도구는 SockJS를 인식하지 못하므로 테스트 자체가 불가능**합니다.
- 실시간 웹 애플리케이션, 채팅, 대시보드, IoT 등 **현대적 환경**에서는 WebSocket만 사용하는 것이 바람직합니다.
