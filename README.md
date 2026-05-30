# 📅 모임 일정 조율 서비스 (Meeting Scheduler)

친구들과 약속을 잡을 때 각자 가능한 날짜를 입력하면 모두 가능한 날짜를 자동으로 계산해주는 RESTful API 서버입니다.

## 🛠 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA |
| Database | MySQL 8 |
| Cache | Redis |
| Build | Gradle |
| API Docs | Swagger (springdoc-openapi) |

## 📌 주요 기능

- **방 생성**: 모임 이름, 날짜 범위 설정 후 링크 공유
- **날짜 입력**: 참여자가 캘린더에서 가능한 날짜 선택
- **최적 날짜 계산**: 전원 가능한 날짜 자동 계산 및 인원순 정렬
- **방 자동 만료**: Redis TTL로 endDate 이후 자동 만료 처리

## 🏗 프로젝트 구조

```
src/main/java/com/example/meeting_schedule
├── domain
│   ├── room
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   └── participant
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       └── dto
└── global
    ├── config
    │   ├── RedisConfig.java
    │   └── SwaggerConfig.java
    └── exception
        ├── BusinessException.java
        ├── ErrorCode.java
        ├── ErrorResponse.java
        └── GlobalExceptionHandler.java
```

## 📡 API 명세

### 방 API

| Method | URL | 설명 |
|---|---|---|
| POST | /api/rooms | 방 생성 |
| GET | /api/rooms/{roomId} | 방 조회 |

### 참여자 API

| Method | URL | 설명 |
|---|---|---|
| POST | /api/rooms/{roomId}/availability | 가능한 날짜 입력 |
| GET | /api/rooms/{roomId}/result | 최적 날짜 결과 |

## 💡 동작 예시

```
방 생성 (날짜 범위: 6/1 ~ 6/30)
    ↓
참여자 입력
이민경: 6/7, 6/14, 6/21 가능
김지수: 6/7, 6/28 가능
철수:   6/7, 6/14 가능
    ↓
결과
6/7  → 전원 가능 ✅
6/14 → 2명 가능
6/28 → 1명 가능
```

## ⚙️ 실행 방법

### 1. MySQL DB 생성

```sql
CREATE DATABASE meeting_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. Redis 실행

```bash
brew services start redis
```

### 3. application.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_db
    username: {MySQL 유저명}
    password: {MySQL 비밀번호}
```

### 4. 서버 실행

```bash
./gradlew bootRun
```

### 5. Swagger UI 접속

```
http://localhost:8080/swagger-ui.html
```

## 🚀 설계 포인트

- **도메인 중심 패키지 구조**: 응집도 높은 설계
- **Redis TTL**: 방 자동 만료 처리 (별도 배치 작업 불필요)
- **UUID**: 링크 공유 특성상 예측 불가능한 id로 보안 강화
- **전역 예외처리**: GlobalExceptionHandler로 일관된 에러 응답
- **덮어쓰기**: 같은 이름으로 재입력 시 기존 날짜 삭제 후 저장