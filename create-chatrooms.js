// docker run -i grafana/k6 run - < create-chatrooms.js

import ws from 'k6/ws';
import http from 'k6/http';

export let options = {
    vus: 1,          // 동시성 1
    iterations: 50,  // 50번 반복 (testuser1 ~ testuser50)
};

export default function () {
    const index = __ITER + 1; // 1 ~ 50
    const email = `testusers${index}@gmail.com`;
    const password = 'Test1234!';

    // 1. 로그인
    const loginRes = http.post('http://host.docker.internal:8080/users/login', JSON.stringify({
        email,
        password
    }), {
        headers: {'Content-Type': 'application/json'}
    });

    const cookies = loginRes.cookies['JSESSIONID'];
    if (!cookies) {
        console.error(`로그인 실패: ${email}, status: ${loginRes.status}, body: ${loginRes.body}`);
        return;
    }
    const sessionId = cookies[0].value;

    // 2. 채팅방 생성 (필요에 따라 body에 chatroomName 등 추가)
    const createRoomRes = http.post(
        'http://host.docker.internal:8080/chatrooms',
        JSON.stringify({chatroomName: `테스트방${index}`}),
        {
            headers: {
                'Content-Type': 'application/json',
                'Cookie': `JSESSIONID=${sessionId}`
            }
        }
    );

    if (createRoomRes.status !== 201 && createRoomRes.status !== 200) {
        console.error(`채팅방 생성 실패: ${email}, status: ${createRoomRes.status}, body: ${createRoomRes.body}`);
        return;
    }

    let chatroomId;
    try {
        chatroomId = JSON.parse(createRoomRes.body).result.chatroomId;
    } catch (e) {
        console.error(`채팅방 생성 응답 파싱 오류: ${email}, body: ${createRoomRes.body}`);
        return;
    }
}