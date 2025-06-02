# DevMountain

DevMountain는 개발자가 다양한 개발 강의를 검색, 주문, 리뷰하며 AI 챗봇과 함께 개인화된 학습 경로를 추천받을 수 있는 온라인 학습 플랫폼입니다.

## 프로젝트 개요

* **목적**: AI 챗봇 기반 추천 시스템을 통해 개발자에게 맞춤형 강의를 제공하고, 유료 구독을 통해 확장 기능을 지원
* **도메인**: 온라인 교육, 추천 시스템, AI 챗봇

---

## 주요 기능 (MVP)

### 사용자 인증

* 회원가입 및 로그인 (OAuth2 or 이메일/비밀번호)
* Spring Security 기반 인증/인가 구현

### 챗봇 기반 강의 추천

* WebSocket 기반 실시간 채팅 구현
* 유저가 입력한 카테고리나 키워드를 기반으로 강의 추천
* 대화 중 언어 경험, 목표 언어 등 정보를 수집해 정밀 추천
* 추천 결과에 S3 이미지 포함, 채팅 기록 DB 저장
* 하루 최대 3회 추천 제한, 유료 구독자는 무제한

### 유료 구독 기능 (보류 중)

* 토스페이 결제 연동
* 결제 시 유저 역할 FREE → PRO 변경
* PRO 유저는 프리미엄 기능 사용 가능 (무제한 추천, 이미지 포함 요청, 개인 설정 유지 등)

### AI 및 기술 요소

* AI 추천 시스템: Spring AI + pgvector(PostgreSQL)
* 데이터 수집 자동화: 크롤링 + S3 저장 + 스케줄러

---

## 기술 스택

* **Backend**: Spring Boot, Spring Security, Spring WebSocket, Spring Data JPA
* **Infra**: Redis, MySQL, Docker
* **AI**: Spring AI, OpenAI API
* **Vector DB**: PostgreSQL + pgvector
* **DevOps**: GitHub Actions or jenkins, Prometheus + Grafana, Slack 알림

---

## 패키지 구조 (프로젝트 기준)

```
nbc.devmountain
├── common
│   ├── config
│   ├── exception
│   ├── handler
│   ├── response
│   └── util
├── domain
│   ├── category.model
│   ├── chat.model
│   ├── common.entity
│   ├── lecture.model
│   ├── order
│   ├── recommendation.model
│   └── user
└── MountainApplication.java
```

* **common**: 글로벌 설정, 공통 응답, 예외 처리 등
* **domain**: 도메인 중심 하위 모듈 분리
* 각 도메인: 모델, 서비스, 컨트롤러, 레포지토리 포함

---

## 부가기능 및 확장 계획

* 추천 수, 클릭 수, 리뷰 수 기반 강의 랭킹 구현
* 추천 커뮤니티 기능 + 유저 간 상호작용
* Slack 연동 경보 시스템 구축 (모니터링)
* 크롤링 자동화 + 스케줄러 적용
* 테스트 코드 작성 및 병합 (TDD 일부 적용)

---

## 실행 환경 및 주의사항
* Docker 환경 기반 실행 (MySQL, Redis 포함)

---

## 배포 및 운영

* Docker 기반 운영 환경 구성
* GitHub Actions or jenkins 를 통한 CI/CD 구축 예정
* 서버 모니터링 및 부하 분산 전략 병행 예정

---