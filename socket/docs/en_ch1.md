# 📌 Redis 기반 채팅 서버 고도화 TO-DO 리스트

## ✅ 1. 유저 상태 관리 기능 추가
- [ ] `ChatEffect.JoinUser` / `ChatEffect.LeaveUser` 추가
- [ ] Redis Set(`room:{roomId}:users`) 기반 유저 리스트 저장 구현
- [ ] 퇴장 시 사용자 제거 및 방이 비면 상태 자동 삭제(optional)
- [ ] `/users/{roomId}` 또는 유저 리스트 구독 기능 추가 고려

## ✅ 2. 메시지 캐싱 기능 추가
- [ ] Redis List(`room:{roomId}:messages`) 기반 최근 메시지 저장
- [ ] TTL or MaxLength 설정 (예: 최신 100개)
- [ ] 메시지 전송 시 캐시 저장 효과(`ChatEffect.CacheMessage`) 추가
- [ ] DB 저장과 분리하여 비동기 처리 고려

## ✅ 3. 타이핑 상태 관리
- [ ] `ChatEffect.Typing` 정의 및 처리 로직 추가
- [ ] Redis Set(`room:{roomId}:typing`)을 이용한 입력 중 유저 관리
- [ ] 일정 시간 경과 시 자동 제거 또는 클라이언트 측 반복 ping

## ✅ 4. 읽음 처리 (Read Receipt)
- [ ] `ChatEffect.ReadReceipt` 정의
- [ ] Redis Hash(`room:{roomId}:read:{messageId}` → userId) 등으로 구현
- [ ] 전체 유저 수 대비 읽은 유저 수 비율 계산 등 UI 활용 가능

## ✅ 5. 사용자 제어 기능 (kick/ban)
- [ ] `ChatEffect.KickUser` / `BanUser` 구현
- [ ] Redis Set(`room:{roomId}:banned`)에 등록
- [ ] 입장 시 차단 여부 확인하여 거부 처리

## ✅ 6. 테스트 및 검증
- [ ] 각 기능별 단위 테스트 작성 (Kotest + Embedded Redis)
- [ ] 통합 테스트: Join → 메시지 전송 → Leave 흐름 검증
- [ ] 동시성 테스트: 다중 유저 join/send 메시지 시 일관성 확인

## ✅ 7. 운영 준비
- [ ] Redis key 구조 명세 문서화 (`room:{roomId}:users`, `:messages`, `:typing`, ...)
- [ ] 메트릭 연동 (e.g. 접속 유저 수, 메시지 수, Redis 키 수)
- [ ] Redis TTL 정책 적용 여부 결정 (inactive room cleanup 등)
