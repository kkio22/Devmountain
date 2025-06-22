# DevMountain nGrinder 성능 테스트 완전 가이드

## 🎯 개요

이 가이드는 DevMountain 프로젝트에서 nGrinder를 사용한 성능 테스트 전체 과정을 설명합니다. 회원가입/로그인 API의 성능을 측정하고 병목점을 찾아 개선하는 것이 목표입니다.

## 📋 전체 구성

```
DevMountain 성능 테스트 환경 (devmountain-perf-net: 172.25.0.0/16)
├── Redis (172.25.0.10:6379)               # 캐시 서버
├── PostgreSQL (172.25.0.20:5432)          # 데이터베이스
├── Spring Boot App (172.25.0.30:8080)     # 테스트 대상 애플리케이션
├── nGrinder Controller (172.25.0.40:8080) # 테스트 관리 및 웹 인터페이스
└── nGrinder Agents × 3 (동적 IP)          # 성능 테스트 실행 (기본 3개)
```

## 🚀 1단계: nGrinder 바이너리 다운로드

### 필수 다운로드 파일
nGrinder 바이너리 파일들은 용량이 크므로 직접 다운로드해야 합니다.

#### 1. nGrinder Controller 다운로드
```bash
cd performance/

# nGrinder Controller 다운로드 (권장 버전: 3.5.9-p1)
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.9-p1-20240613/ngrinder-controller-3.5.9-p1.war

# 또는 안정 버전 3.5.8 사용 시
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.8/ngrinder-controller-3.5.8.war
```

#### 2. nGrinder Agent 다운로드
```bash
# nGrinder Agent 다운로드 (Controller와 동일한 버전 필수)
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.9-p1-20240613/ngrinder-agent-3.5.9-p1-localhost.tar

# 또는 안정 버전 3.5.8 사용 시
wget https://github.com/naver/ngrinder/releases/download/ngrinder-3.5.8/ngrinder-agent-3.5.8-localhost.tar
```

#### 3. 다운로드 완료 확인
```bash
ls -la performance/
# 필수 파일들 확인:
# - ngrinder-controller-*.war
# - ngrinder-agent-*.tar
# - ngrinder-Dockerfile
# - agent-Dockerfile
# - SignUpAndLoginTestRunner.groovy
```

### ⚠️ 버전 호환성 주의사항
- Controller와 Agent는 **반드시 같은 버전**을 사용해야 합니다
- Dockerfile 내의 COPY 명령어가 다운로드한 파일명과 일치하는지 확인하세요

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

#### 기본 실행 (Agent 3개)
```bash
# 루트 디렉토리에서 실행
docker compose -f docker-compose.performance.yaml up -d
```

#### Agent 개수 조정 실행
```bash
# Agent 1개로 실행
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=1

# Agent 5개로 실행 (고부하 테스트용)
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=5

# Agent 10개로 실행 (최대 부하 테스트용)
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=10
```

### 컨테이너 상태 확인
```bash
# 모든 컨테이너 상태 확인
docker compose -f docker-compose.performance.yaml ps

# Agent 컨테이너들만 확인
docker ps --filter "name=ngrinder-agent"

# 컨테이너 로그 확인
docker compose -f docker-compose.performance.yaml logs -f ngrinder-controller
docker compose -f docker-compose.performance.yaml logs ngrinder-agent
```

## 🔧 3단계: nGrinder 설정

### 1. nGrinder Controller 접속
- **URL**: http://localhost:8081
- **기본 계정**: admin / admin

### 2. Agent 연결 확인
1. nGrinder 웹 인터페이스 로그인
2. **Management** → **Agent Management** 메뉴 이동
3. 모든 Agent가 **approved** 상태인지 확인
4. 상태가 **unapproved**인 경우 각각 **Approve** 버튼 클릭

### 3. 시스템 상태 점검
- **Status**: 모든 Agent가 **Ready** 상태여야 함
- **Region**: localhost로 설정되어 있어야 함
- **Total Agents**: 실행한 Agent 개수와 일치해야 함 (기본 3개)

## 📝 4단계: 테스트 스크립트 업로드

### SignUpAndLoginTestRunner.groovy 특징
현재 제공되는 테스트 스크립트의 주요 특징:

#### 테스트 시나리오
1. **회원가입 API 테스트**
   ```
   POST http://devmountain-app:8080/users/signup
   ```
   - 스레드별 고유한 이메일/전화번호로 사용자 생성
   - 타임스탬프 기반 중복 방지
   - 응답 코드: 200/201 (성공), 400/409 (중복/검증 오류) 처리

2. **로그인 API 테스트**
   ```
   POST http://devmountain-app:8080/users/login
   ```
   - 회원가입에서 생성한 동일한 사용자 정보로 로그인
   - 응답 코드: 200 (성공), 401/400 (인증 실패) 처리

#### 고유 데이터 생성 방식
```groovy
// 각 스레드별 고유 사용자 생성
def timestamp = System.currentTimeMillis()
def uniqueEmail = "testuser${grinder.threadNumber}_${timestamp}@example.com"
def phoneNumber = "010-${String.format('%04d', grinder.threadNumber)}-${String.format('%04d', timestamp % 10000)}"
```

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
   - **Agent**: 사용 가능한 Agent 개수 확인 (기본 3개)

### 권장 테스트 시나리오

#### 🔰 초기 테스트 (1 Agent 사용)
```
Virtual Users: 10명
Duration: 2분
Ramp-up Period: 10초
Agent Count: 1개
```

#### 📈 중간 테스트 (3 Agent 사용 - 기본값)
```
Virtual Users: 30명 (Agent당 10명)
Duration: 5분
Ramp-up Period: 30초
Agent Count: 3개
```

#### 💪 고부하 테스트 (5 Agent 사용)
```bash
# 먼저 Agent를 5개로 확장
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=5

# 테스트 설정:
Virtual Users: 100명 (Agent당 20명)
Duration: 10분
Ramp-up Period: 1분
Agent Count: 5개
```

#### 🚀 최대 부하 테스트 (10 Agent 사용)
```bash
# Agent를 10개로 확장
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=10

# 테스트 설정:
Virtual Users: 200명 (Agent당 20명)
Duration: 15분
Ramp-up Period: 2분
Agent Count: 10개
```

### 실시간 Agent 스케일링
테스트 중에도 Agent 개수를 조정할 수 있습니다:
```bash
# 테스트 중 Agent 개수 증가
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=7

# 테스트 완료 후 Agent 개수 감소
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=2
```

### 테스트 실행 및 모니터링
1. **Start** 버튼으로 테스트 시작
2. **Real-time Monitoring**에서 진행 상황 확인:
   - **TPS (Transactions Per Second)**: 초당 처리 트랜잭션 수
   - **Response Time**: 평균/최대 응답 시간
   - **Error Rate**: 오류 발생률
   - **Active Virtual Users**: 현재 활성 사용자 수
   - **Active Agents**: 현재 활성 Agent 수

## 📊 6단계: 결과 분석

### 주요 지표 해석

#### 성공 기준 (Agent 3개 기준)
- **TPS**: 150+ (Agent당 50+ TPS)
- **평균 응답 시간**: 1초 이하
- **성공률**: 95% 이상
- **에러율**: 5% 이하
- **Agent 활용률**: 모든 Agent 정상 동작

#### Agent 개수별 예상 성능
| Agent 개수 | 권장 VUser | 예상 TPS | 사용 시나리오 |
|------------|------------|----------|---------------|
| 1개        | 10-20명    | 50+ TPS  | 기본 안정성 테스트 |
| 3개        | 30-60명    | 150+ TPS | 일반 부하 테스트 |
| 5개        | 50-100명   | 250+ TPS | 중간 부하 테스트 |
| 10개       | 100-200명  | 500+ TPS | 최대 부하 테스트 |

#### 분석 포인트
1. **Response Time Graph**: 응답 시간 추이 확인
2. **TPS Graph**: 처리량 변화 분석  
3. **Error Statistics**: 오류 유형별 분석
4. **Agent Performance**: Agent별 성능 분포 확인
5. **Detailed Report**: HTTP 상태 코드별 상세 분석

### 성능 개선 방향
- **응답 시간 개선**: 데이터베이스 쿼리 최적화, 인덱스 추가
- **처리량 증대**: 커넥션 풀 조정, 캐시 활용
- **오류율 감소**: 예외 처리 개선, 입력 검증 강화
- **확장성 개선**: 로드 밸런싱, 마이크로서비스 분리

## 🔧 트러블슈팅

### 일반적인 문제 해결

#### 1. Agent가 연결되지 않는 경우
```bash
# Agent 컨테이너 로그 확인
docker compose -f docker-compose.performance.yaml logs ngrinder-agent

# Controller 컨테이너 로그 확인  
docker compose -f docker-compose.performance.yaml logs ngrinder-controller

# 네트워크 연결 확인
docker exec devmountain-ngrinder-agent-1 ping 172.25.0.40
```

#### 2. Agent 개수가 인식되지 않는 경우
```bash
# 현재 실행 중인 Agent 확인
docker ps --filter "name=ngrinder-agent"

# Agent 재시작
docker compose -f docker-compose.performance.yaml restart

# 특정 개수로 다시 시작
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=5
```

#### 3. 테스트 대상 서버에 연결할 수 없는 경우
```bash
# App 컨테이너 상태 확인
docker ps | grep devmountain-app-perf

# App 로그 확인
docker compose -f docker-compose.performance.yaml logs devmountain-app-perf

# 네트워크 연결 테스트
docker exec devmountain-ngrinder-agent-1 curl http://172.25.0.30:8080/actuator/health
```

#### 4. 성능이 예상보다 낮은 경우
- Docker 리소스 제한 확인: `docker stats`
- 호스트 시스템 리소스 확인: `top`, `htop`
- Agent 개수 증가: `--scale ngrinder-agent=10`
- 데이터베이스 성능 확인

### 다른 PC에서 사용 시 설정 변경

#### IP 주소 변경이 필요한 경우
1. `docker-compose.performance.yaml`의 서브넷 변경:
   ```yaml
   # 예: 172.25.0.0/16 → 172.26.0.0/16
   networks:
     devmountain-perf-net:
       ipam:
         config:
           - subnet: 172.26.0.0/16
   ```

2. 모든 서비스의 고정 IP 변경:
   - Redis: `172.25.0.10` → `172.26.0.10`
   - PostgreSQL: `172.25.0.20` → `172.26.0.20`
   - App: `172.25.0.30` → `172.26.0.30`
   - nGrinder Controller: `172.25.0.40` → `172.26.0.40`

3. `SignUpAndLoginTestRunner.groovy`의 baseUrl은 자동으로 해결됩니다:
   ```groovy
   public static String baseUrl = "http://devmountain-app:8080"  # Docker 내부 DNS 사용
   ```

## 🎛️ 고급 사용법

### 동적 Agent 관리
```bash
# 테스트 진행 중 Agent 추가
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=8

# 메모리 절약을 위해 Agent 감소
docker compose -f docker-compose.performance.yaml up -d --scale ngrinder-agent=2

# 모든 Agent 중지 (Controller는 유지)
docker compose -f docker-compose.performance.yaml stop ngrinder-agent
```

### 성능 모니터링
```bash
# 실시간 컨테이너 리소스 모니터링
docker stats

# 네트워크 트래픽 확인
docker exec devmountain-app-perf ss -tuln

# 데이터베이스 커넥션 확인
docker exec devmountain-postgres-perf psql -U postgres -d devmountain -c "SELECT count(*) FROM pg_stat_activity;"
```

## 📚 추가 참고자료

- [nGrinder 공식 문서](https://naver.github.io/ngrinder/)
- [nGrinder GitHub 릴리스](https://github.com/naver/ngrinder/releases)
- [Docker Compose Scale 가이드](https://docs.docker.com/compose/reference/up/)
- [Docker 네트워크 관리](https://docs.docker.com/compose/networking/)

## 💡 팁 & 베스트 프랙티스

### 성능 테스트 전략
1. **점진적 부하 증가**: 
   - 1 Agent → 3 Agent → 5 Agent → 10 Agent 순으로 단계적 증가
2. **Agent 개수 최적화**: 
   - CPU 코어 수의 1-2배 정도가 적정
   - 메모리가 부족한 경우 Agent 개수 조정
3. **테스트 데이터 관리**: 
   - 대량 테스트 후 불필요한 사용자 데이터 정리
   - 데이터베이스 용량 모니터링

### 리소스 최적화
```bash
# 테스트 완료 후 정리
docker compose -f docker-compose.performance.yaml down

# 볼륨까지 함께 정리 (데이터 삭제 주의)
docker compose -f docker-compose.performance.yaml down -v

# 사용하지 않는 이미지 정리
docker image prune -f
```

### 모니터링 체크리스트
- [ ] 모든 Agent가 Controller에 연결되어 있는가?
- [ ] 테스트 대상 애플리케이션이 정상 응답하는가?
- [ ] 호스트 시스템 리소스(CPU/메모리)가 충분한가?
- [ ] 네트워크 연결이 안정적인가?
- [ ] 데이터베이스 커넥션 풀이 적절히 설정되어 있는가?

---

**🎉 다중 Agent를 활용한 확장 가능한 성능 테스트 환경으로 DevMountain의 성능 한계를 정확히 측정하세요!** 