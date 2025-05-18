# WebSocket 채팅 클라이언트

Vue 3와 TypeScript를 사용하여 구현된 실시간 WebSocket 채팅 클라이언트입니다.

## 기술 스택

- Vue 3
- TypeScript
- Vite
- STOMP.js
- TailwindCSS

## 주요 기능

- 실시간 WebSocket 통신
- 채팅방 입장/퇴장
- 1:1 메시지 전송
- 실시간 메시지 수신
- 반응형 UI

## 시작하기

### 필수 조건

- Node.js 18.0.0 이상
- npm 또는 yarn

### 설치

```bash
# 의존성 설치
npm install
```

### 개발 서버 실행

```bash
npm run dev
```

### 빌드

```bash
npm run build
```

## 프로젝트 구조

```
src/
  ├── components/     # Vue 컴포넌트
  ├── assets/        # 정적 자원
  └── App.vue        # 루트 컴포넌트
```

## 사용 방법

1. 사용자 ID와 채팅방 ID를 입력합니다.
2. '연결하기' 버튼을 클릭하여 WebSocket 서버에 연결합니다.
3. 메시지를 보낼 상대방의 ID와 메시지 내용을 입력합니다.
4. '전송' 버튼을 클릭하거나 Enter 키를 눌러 메시지를 전송합니다.

## 개발 스크립트

- `npm run dev`: 개발 서버 실행
- `npm run build`: 프로덕션 빌드
- `npm run type-check`: TypeScript 타입 체크
- `npm run lint`: ESLint를 사용한 코드 린팅
- `npm run format`: Prettier를 사용한 코드 포맷팅
