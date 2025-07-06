import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 50,
    iterations: 50,
    thresholds: {
        http_req_duration: ['p(95)<5000'],
        http_req_failed: ['rate<0.1'],
        checks: ['rate>0.95']
    }
};

const queries = [
    '나는 지금 자바를 배우고 있고, 초급 단계야 그리고 프로젝트를 하려고 해 강의 추천해줘 ',
    '나는 데브옵스를 공부하고 클라우드 개발자를 희망해 근데 ec2를 처음 배우는데 프로젝트를 시작하려고 해서 맞는 강의를 추천해줘',
    '나는 react를 공부하고 프론트 개발자를 희망해 react를 처음 배우는데 취업 준비를 시작하려고 해서 맞는 강의를 추천해줘',
    '나는 ai를 공부하고 머신러닝 개발자를 희망해 ai를 처음 배우는데 챗봇을 하나 만드려보려고 하고 있어 맞는 강의를 추천해줘',
    '나는 유니티를 공부하고 있어, 게임 개발자가 되고 싶어 처음 시작해서 초급을 원해 취업 준비를 하려고 해 맞는 강의 추천해줘 '
];

export default function () {
    const index = __VU - 1;
    const email = `testusers${index + 1}@gmail.com`;
    const password = 'Test1234!';
    const roomId = 52 + index;
    const query = queries[index % queries.length];

    const loginRes = http.post('http://host.docker.internal:8080/users/login',
        JSON.stringify({ email, password }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    const loginSuccess = check(loginRes, {
        '로그인 응답 상태 코드 200': (res) => res.status === 200,
        'Set-Cookie 존재 여부': (res) => res.cookies && res.cookies['JSESSIONID'] && res.cookies['JSESSIONID'].length > 0
    });

    if (!loginSuccess) {
        console.error(`❌ 로그인 실패 또는 쿠키 없음: ${email} / status=${loginRes.status}`);
        return;
    }

    const sessionId = loginRes.cookies['JSESSIONID'][0].value;

    const wsUrl = `ws://host.docker.internal:8080/ws/chat?roomId=${roomId}`;
    const wsParams = {
        headers: {
            'Cookie': `JSESSIONID=${sessionId}`
        }
    };

    ws.connect(wsUrl, wsParams, function (socket) {
        socket.on('open', function () {
            console.log(`✅ 연결됨: ${email} / roomId=${roomId}`);

            // 1. "안녕" 먼저 전송
            socket.send("안녕");

            // 2. 약간의 딜레이 후 질문 전송 (ex: 500ms 후)
            socket.setTimeout(() => {
                socket.send(query);
                console.log(`📨 질문 보냄: ${query}`);
            }, 500);
        });

        socket.on('message', function (msg) {
            console.log(`📩 [${email}] 응답: ${msg}`);
        });

        socket.on('close', function () {
            console.log(`🛑 종료됨: ${email}`);
        });

        socket.setTimeout(() => socket.close(), 8000);
    });

    sleep(1);
}