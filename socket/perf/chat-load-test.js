import ws from 'k6/ws';
import { check, sleep } from 'k6';

export const options = {
    vus: 100, // Virtual Users
    duration: '30s', // 30초 동안 유지
};

const BASE_URL = 'ws://localhost:8080/ws';
const roomId = 'test-room';

export default function () {
    const userId = __VU; // Virtual User ID

    const res = ws.connect(BASE_URL, {}, function (socket) {
        socket.on('open', () => {
            console.log(`✅ VU#${userId} 연결됨`);

            // 1. STOMP CONNECT 전송
            socket.send(
                'CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000'
            );

            // 2. STOMP 응답 수신
            socket.on('message', (msg) => {
                if (msg.startsWith('CONNECTED')) {
                    console.log(`🟢 STOMP 연결 성공: VU#${userId}`);

                    // 3. 구독 요청 (subscribe)
                    socket.send(
                        `SUBSCRIBE\nid:sub-0\ndestination:/topic/chatroom/${roomId}\n\n\u0000`
                    );

                    // 4. 입장 메시지 전송 (join)
                    const joinBody = JSON.stringify({ userId: userId });
                    socket.send(
                        `SEND\ndestination:/app/chat/join/${roomId}\ncontent-type:application/json\ncontent-length:${joinBody.length}\n\n${joinBody}\u0000`
                    );

                    // 5. 채팅 메시지 전송 (message)
                    const chatBody = JSON.stringify({
                        from: userId,
                        to: 9999,
                        content: '🔥 k6에서 보냅니다',
                    });
                    socket.send(
                        `SEND\ndestination:/app/chat/message/${roomId}\ncontent-type:application/json\ncontent-length:${chatBody.length}\n\n${chatBody}\u0000`
                    );
                } else {
                    console.log(`📥 VU#${userId} 응답: ${msg}`);
                }
            });

            // 6. 테스트 지속 시간
            sleep(3);
            socket.close();
        });

        socket.on('close', () => {
            console.log(`❎ 연결 종료: VU#${userId}`);
        });

        socket.on('error', (e) => {
            console.error(`🔥 오류: VU#${userId} → ${e.error()}`);
        });
    });

    check(res, {
        'WebSocket 연결 성공': (r) => r && r.status === 101,
    });
}
