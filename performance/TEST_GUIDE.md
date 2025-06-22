# DevMountain nGrinder 성능 테스트 완전 가이드

## 🎯 개요

이 가이드는 DevMountain 프로젝트에서 nGrinder를 사용한 성능 테스트 전체 과정을 설명합니다. 회원가입/로그인 API의 성능을 측정하고 병목점을 찾아 개선하는 것이 목표입니다.

## 📋 전체 구성

```
DevMountain 성능 테스트 환경
├── Spring Boot App (172.25.0.30:8080)     # 테스트 대상
├── PostgreSQL (postgres:5432)             # 데이터베이스
├── Redis (redis:6379)                     # 캐시
├── nGrinder Controller (172.25.0.40:8080) # 테스트 관리
└── nGrinder Agent (172.25.0.50)           # 테스트 실행
```

## 🚀 1단계: nGrinder 바이너리 다운로드

### 필수 다운로드 파일
nGrinder 바이너리 파일들은 용량이 크므로 직접 다운로드해야 합니다.

#### 1. nGrinder Controller 다운로드
1. [nGrinder 릴리스 페이지](https://github.com/naver/ngrinder/releases)에서 최신 버전 확인
2. **권장 버전**: `ngrinder-3.5.9-p1` (최신 보안 업데이트 포함)
3. `ngrinder-controller-3.5.9-p1.war` 파일 다운로드
4. `performance/` 디렉토리에 저장

```bash
# performance 디렉토리로 이동
cd performance/

# nGrinder Controller 다운로드 (최신 버전)
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.9-p1-20240613/ngrinder-controller-3.5.9-p1.war

# 또는 기존 3.5.8 버전 사용 시
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.8/ngrinder-controller-3.5.8.war
```

#### 2. nGrinder Agent 다운로드
```bash
# nGrinder Agent 다운로드 (최신 버전)
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.9-p1-20240613/ngrinder-agent-3.5.9-p1-localhost.tar

# 또는 기존 3.5.8 버전 사용 시
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.8/ngrinder-agent-3.5.8-localhost.tar
```

#### 3. 다운로드 완료 확인
```bash
ls -la performance/
# 다음 파일들이 있어야 함:
# - ngrinder-controller-3.5.9-p1.war (또는 3.5.8)
# - ngrinder-agent-3.5.9-p1-localhost.tar (또는 3.5.8)
# - ngriner-Dockerfile
# - agent-Dockerfile
# - SignUpAndLoginTestRunner.groovy
```

### ⚠️ 버전 호환성 주의사항
- Controller와 Agent는 **반드시 같은 버전**을 사용해야 합니다
- Dockerfile 내의 파일명이 다운로드한 파일명과 일치하는지 확인하세요
- 다른 버전 사용 시 Dockerfile 수정이 필요할 수 있습니다

## 🐳 2단계: Docker 환경 구성

### 환경 파일 설정
프로젝트 루트에 `.env` 파일 생성:
```bash
# .env 파일 내용
REDIS_PORT=6379
POSTGRES_PORT=5432
APP_PORT=8080
POSTGRES_PASSWORD=yourpassword
POSTGRES_DB=devmountain
```

### Docker Compose 실행
```bash
# 루트 디렉토리에서 실행

# 1. 개발용 (nGrinder 없이)
docker-compose up -d

# 2. 성능 테스트용 (nGrinder 포함)
docker-compose -f docker-compose.performance.yaml up -d
```

### 컨테이너 상태 확인
```bash
# 모든 컨테이너가 실행 중인지 확인
docker-compose -f docker-compose.performance.yaml ps

# 로그 확인
docker-compose -f docker-compose.performance.yaml logs -f ngrinder-controller
```

## 🔧 3단계: nGrinder 설정

### 1. nGrinder Controller 접속
- **URL**: http://localhost:8081
- **기본 계정**: admin / admin

### 2. Agent 연결 확인
1. nGrinder 웹 인터페이스 로그인
2. **Management** → **Agent Management** 메뉴 이동
3. Agent가 **approved** 상태인지 확인
4. 상태가 **unapproved**인 경우 **Approve** 버튼 클릭

### 3. 시스템 상태 점검
- **Status**: Agent가 **Ready** 상태여야 함
- **Region**: localhost로 설정되어 있어야 함
- **IP**: 172.25.0.50으로 표시되어야 함

## 📝 4단계: 테스트 스크립트 업로드

### SignUpAndLoginTestRunner.groovy 설명
현재 제공되는 테스트 스크립트는 다음과 같은 시나리오를 실행합니다:

#### 테스트 플로우
1. **회원가입 API 테스트**
   ```
   POST http://172.25.0.30:8080/users/signup
   ```
   - 고유한 이메일/전화번호로 사용자 생성
   - 응답 코드 200 (성공) 또는 400/409 (중복/검증 오류) 확인

2. **로그인 API 테스트**
   ```
   POST http://172.25.0.30:8080/users/login
   ```
   - 생성된 사용자 정보로 로그인 시도
   - 응답 코드 200 (성공) 또는 401 (인증 실패) 확인

### 스크립트 업로드 방법
1. nGrinder 웹 인터페이스에서 **Script** 메뉴 클릭
2. **Create a script** 버튼 클릭
3. **Upload** 탭에서 `SignUpAndLoginTestRunner.groovy` 파일 업로드
4. **Validate** 버튼으로 스크립트 검증
5. 검증 성공 후 **Save** 클릭

## 🎯 5단계: 성능 테스트 실행

### 테스트 생성
1. **Performance Test** 메뉴 → **Create Test** 클릭
2. 테스트 설정:
   - **Test Name**: "DevMountain 회원가입/로그인 성능 테스트"
   - **Script**: SignUpAndLoginTestRunner.groovy 선택
   - **Agent**: 1개 (기본값)

### 권장 테스트 시나리오

#### 🔰 초기 테스트 (안정성 확인)
```
Virtual Users: 10명
Duration: 2분
Ramp-up Period: 10초
```

#### 📈 중간 테스트 (기본 부하)
```
Virtual Users: 50명
Duration: 5분
Ramp-up Period: 30초
```

#### 💪 부하 테스트 (한계 확인)
```
Virtual Users: 100-200명
Duration: 10분
Ramp-up Period: 1분
```

### 테스트 실행 및 모니터링
1. **Start** 버튼으로 테스트 시작
2. **Real-time Monitoring**에서 진행 상황 확인:
   - TPS (Transactions Per Second)
   - Response Time
   - Error Rate
   - Active Virtual Users

## 📊 6단계: 결과 분석

### 주요 지표 해석

#### 성공 기준
- **TPS**: 목표값에 따라 다름 (예: 100 TPS)
- **평균 응답 시간**: 1초 이하
- **성공률**: 95% 이상
- **에러율**: 5% 이하

#### 분석 포인트
1. **Response Time Graph**: 응답 시간 추이 확인
2. **TPS Graph**: 처리량 변화 분석
3. **Error Statistics**: 오류 유형별 분석
4. **Detailed Report**: HTTP 상태 코드별 상세 분석

### 성능 개선 방향
- **응답 시간 개선**: 데이터베이스 쿼리 최적화, 인덱스 추가
- **처리량 증대**: 커넥션 풀 조정, 캐시 활용
- **오류율 감소**: 예외 처리 개선, 입력 검증 강화

## 🔧 트러블슈팅

### 일반적인 문제 해결

#### 1. Agent가 연결되지 않는 경우
```bash
# Agent 컨테이너 로그 확인
docker logs ngrinder_agent

# Controller 컨테이너 로그 확인
docker logs ngrinder_controller

# 네트워크 연결 확인
docker exec ngrinder_agent ping 172.25.0.40
```

#### 2. 테스트 대상 서버에 연결할 수 없는 경우
```bash
# App 컨테이너 상태 확인
docker ps | grep devmountain-app

# App 로그 확인
docker logs devmountain-app

# 네트워크 연결 테스트
curl http://172.25.0.30:8080/health
```

#### 3. 성능이 예상보다 낮은 경우
- Docker 컨테이너 리소스 제한 확인
- 호스트 시스템의 CPU/메모리 사용률 확인
- 데이터베이스 커넥션 풀 설정 검토

### 다른 PC에서 사용 시 설정 변경

#### IP 주소 변경이 필요한 경우
1. `docker-compose.performance.yaml`의 서브넷 변경:
   ```yaml
   # 예: 172.25.0.0/16 → 172.26.0.0/16
   networks:
     devmonntain-net:
       ipam:
         config:
           - subnet: 172.26.0.0/16
   ```

2. 모든 서비스의 고정 IP 변경:
   - App: `172.25.0.30` → `172.26.0.30`
   - nGrinder Controller: `172.25.0.40` → `172.26.0.40`
   - nGrinder Agent: `172.25.0.50` → `172.26.0.50`

3. `SignUpAndLoginTestRunner.groovy`의 테스트 URL 변경:
   ```groovy
   def url = "http://172.26.0.30:8080"  # 새 IP로 변경
   ```

## 📚 추가 참고자료

- [nGrinder 공식 문서](https://naver.github.io/ngrinder/)
- [nGrinder GitHub 릴리스](https://github.com/naver/ngrinder/releases)
- [Docker Compose 네트워크 가이드](https://docs.docker.com/compose/networking/)

## 💡 팁 & 베스트 프랙티스

1. **점진적 부하 증가**: 작은 부하부터 시작해서 점진적으로 증가
2. **테스트 데이터 정리**: 대량 테스트 후 불필요한 사용자 데이터 정리
3. **모니터링 병행**: 성능 테스트 중 서버 리소스 모니터링 필수
4. **베이스라인 설정**: 정기적인 성능 테스트로 성능 변화 추적

---

**🎉 성공적인 성능 테스트를 위해 이 가이드를 차근차근 따라하세요!** 