# kotlin-socket-fp

코틀린 기반의 소켓 서버를 함수형으로 구현한 프로젝트입니다.

## 프로젝트 구성

이 프로젝트는 다음과 같은 하위 프로젝트들로 구성되어 있습니다:

- [socket](./socket/README.md) - 코틀린 기반의 WebSocket 서버 구현
- [client](./client/README.md) - Vue 3 기반의 WebSocket 클라이언트 구현

## 시작하기

각 프로젝트의 자세한 설치 및 실행 방법은 해당 프로젝트의 README.md를 참고해주세요.

### 서버 실행

```bash
cd socket
./gradlew bootRun
```

### 클라이언트 실행

```bash
cd client
npm install
npm run dev
```
