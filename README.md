# DevMountain

> AI 기반 맞춤형 강의 추천 플랫폼

---

## 서비스 개요

> 개발 공부를 시작하면서, 어떤 강의를 선택해야 할지 몰라 고민만 하다 소중한 시간을 허비했던 경험이 있으신가요?  
> DevMountain은 이러한 고민을 해결하고, 여러분의 소중한 시간을 아껴드리기 위해 탄생한 맞춤형 AI 강의 추천 플랫폼입니다.

### 핵심 가치
- **개인화 추천**: 사용자의 관심 기술, 학습 목표, 현재 수준을 정밀하게 분석
- **통합 검색**: 인프런, 유튜브, 웹 검색 등 다양한 플랫폼의 강의를 한곳에서
- **실시간 상호작용**: WebSocket 기반의 자연스러운 AI 챗봇 대화

---

## 주요 기능 상세

### 🤖 AI 강의 추천 챗봇
- **OpenAI 임베딩 모델** 기반 사용자 입력과 유사한 강의 자동 추천
- **회원 전용**: Web Search API + YouTube MCP를 통한 외부 강의 추천
- **PRO 전용**: 가격 필터링, 강의 피드백 기능 제공
- **단계적 추천**: 배우고 싶은 기술 → 학습 목표 →  난이도 → 최종 결과 출력

### 💬 실시간 채팅 시스템
- **WebSocket 기반** 실시간 응답 및 자연스러운 대화 흐름
- **게스트/회원 구분**, 1:1 채팅방 구성 및 권한 기반 기능 제공
- **타이핑 효과** 적용으로 실제 채팅과 같은 경험
- **대화 이력 저장**: 모든 대화 및 추천 내역 DB 저장

### 🎯 회원 전용 기능
- **채팅방 분리**: 대화 주제별로 효율적인 학습 흐름 관리
- **이력 관리**: 이전 대화 및 추천 내역 확인 기능
- **강의 카드**: 썸네일, 설명, 링크 포함 (비회원은 텍스트 기반)

---

## 서비스 플로우

### 게스트 사용자
1. 웹사이트 접속
2. AI 챗봇과 즉시 대화 시작
3. 텍스트 기반 강의 추천 받기
4. 제한된 기능으로 기본 서비스 체험

### 회원 사용자
1. 회원가입/로그인 (OAuth2.0 지원)
2. 개인 채팅방 생성 및 관리
3. 강의 카드 형태의 상세 추천
4. 대화 이력 저장 및 관리
5. Web Search API, YouTube MCP 활용

### PRO 사용자
1. 무제한 메시지 전송
2. 가격 필터링 기능 활용
3. 추천 강의에 대한 상세 피드백
4. 고급 AI 기능 체험

---

## PRO 등급 체험 안내

> **Brave Search API 키를 제공해주시면 DevMountain의 모든 PRO 기능을 무료로 체험하실 수 있습니다!**

### ⭐ PRO 등급 혜택
- **무제한 메시지 전송** - 일반 회원은 하루 3회 제한
- **금액 필터 기능** - 원하는 가격대의 강의만 추천받기
- **추천 강의 피드백 기능** - AI가 추천한 강의에 대한 상세 질문 가능

### 🎁 참여 방법
1. **[API 키 발급 가이드 확인하기](https://velog.io/@hyang_do/Devmountain%EC%9D%84-%EC%9C%84%ED%95%9C-Brave-%ED%82%A4-%EB%B0%9C%EA%B8%89-%EB%B0%A9%EB%B2%95-%EC%95%88%EB%82%B4)**
2. **[API 키 신청하기](https://docs.google.com/forms/d/e/1FAIpQLSeRo1JLXXB3wqar0u-8R1BjLKl-VeobjbWAWmkXDEHyMnT1bw/viewform)**

---

## 🛠️ 기술 스택

### 🖥️ Backend
![Java](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=spring-boot)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.x-6DB33F?logo=spring)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-4.x-6DB33F?logo=spring)
![Spring Security](https://img.shields.io/badge/Spring%20Security-5.x-6DB33F?logo=spring-security)
![Spring AI](https://img.shields.io/badge/Spring%20AI-Library-6DB33F?logo=spring)

### 🗄️ Database & Cache
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?logo=redis&logoColor=white)

### ☁️ Infrastructure & DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=github-actions&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazon-aws&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?logo=grafana&logoColor=white)

### 🔌 External APIs
![OpenAI](https://img.shields.io/badge/OpenAI-412991?logo=openai&logoColor=white)
![Brave Search](https://img.shields.io/badge/Brave%20Search-FF2000?logo=brave&logoColor=white)
![Toss Payments](https://img.shields.io/badge/Toss%20Payments-0064FF?logo=toss&logoColor=white)

### 🎨 Frontend & Tools
![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?logo=vue.js&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?logo=intellijidea&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?logo=postman&logoColor=white)

---

## 📁 패키지 구조

```
src/
├── main/
│   ├── java/nbc/devmountain/
│   │   ├── common/          # 공통 모듈
│   │   │   ├── config/      # 설정
│   │   │   ├── exception/   # 예외 처리
│   │   │   ├── response/    # 응답 형식
│   │   │   └── util/        # 유틸리티
│   │   │       ├── oauth2/  # OAuth2 인증
│   │   │       ├── ratelimit/ # Rate Limiting
│   │   │       └── security/  # 보안
│   │   └── domain/          # 도메인별 비즈니스 로직
│   │       ├── ai/          # AI 챗봇
│   │       ├── chat/        # 채팅
│   │       ├── lecture/     # 강의
│   │       ├── order/       # 주문/결제
│   │       ├── user/        # 사용자
│   │       └── search/      # 검색
│   └── resources/
└── test/
```

---

## 주요 기술적 의사결정

- Spring AI + OpenAI GPT-4o Mini
- PgVectorStore 기반 임베딩 추천
- WebSocket 실시간 채팅
- Spring Batch 대용량 크롤링/임베딩
- Toss Payments 결제/구독
- Brave Search API 실시간 트렌드
- Redis Stack 벡터 캐싱
- Zookeeper 기반 키 관리
- CI/CD 자동화(Docker, GitHub Actions, AWS EC2)
- 모니터링(Grafana, Prometheus, Slack 연동)

---

## 트러블슈팅/개선 사례

- Selenium 크롤링 불안정 → 공식 API 방식 전환
- Spring Batch 테이블 생성 이슈 → Flyway 마이그레이션 도입
- 외부 서비스 Key 제한 → Zookeeper 기반 실시간 키 교체
- Redis 연동 Rate Limit 충돌 → Filter 등록 및 시그니처 정비

---

## 팀원 및 역할 분담

| 이름     | 역할/담당 |
|----------|-----------------------------|
| 이윤혁   | AI 챗봇 기능 개발, pg vector 사용, 가격 필터, 사용자 실시간 피드백을 통한 강의 재추천 |
| 윤소현   | Spring Security + OAuth2 + session(인증인가), Rate Limit 설정, 모니터링 시각화, 프론트 |
| 류형철   | 웹소켓 채팅, AI 응답 포맷팅, 채팅방 이름 설정, 타이핑 효과 |
| 김나현   | Spring Batch 사용, 데이터 크롤링 & 임베딩, 캐싱, Git Action으로 스케줄링, Slack 알림 & 모니터링  |
| 박화랑   | 키 스와핑, 토스 API 연동, CI/CD, Docker, Brave Search, MCP client 구축, 모니터링, Zookeeper |

---

## 회의록 및 문서

### 프로젝트 문서
- [ERD 설계](https://www.erdcloud.com/d/xvcpvfeg59EAM6Xri)
- [Figma 와이어프레임](https://www.figma.com/design/WtFELy49m1xkLrWcycSOSb/Devmountain?node-id=13-247&p=f&m=draw)
- [API 명세서](https://www.notion.so/teamsparta/2262dc3ef5148157b485e2e53a71895e?v=2262dc3ef51481fdb91c000c54e694f8)

---
