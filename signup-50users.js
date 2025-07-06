// docker run --rm -i grafana/k6 run - < signup-50users.js

import http from 'k6/http';

export let options = {
    vus: 1,         // 1명(VU)만 사용
    iterations: 50, // 50회만 반복 (회원가입 50개)
};

export default function () {
    const index = __ITER + 1; // 1~50 반복
    const email = `testusers${index}@gmail.com`;
    const password = 'Test1234!';
    const name = `테스트유저${index}`;
    const phoneNumber = '010333333333';
    const role = 'ADMIN'

    const payload = JSON.stringify({
        email: email,
        password: password,
        name: name,
        phoneNumber:phoneNumber,
        role:role
    });

    const headers = { 'Content-Type': 'application/json' };

    let res = http.post('http://host.docker.internal:8080/users/signup', payload, { headers });

    if (res.status === 201 || res.status === 200) {
        console.log(`회원가입 성공: ${email}`);
    } else {
        console.error(`회원가입 실패: ${email}, 응답코드: ${res.status}, 응답: ${res.body}`);
    }
}