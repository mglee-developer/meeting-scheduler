# 📅 모임 일정 조율 서비스 (Meeting Scheduler)

친구, 지인, 동호회 등 여러 사람이 약속을 잡을 때 각자 가능한 날짜를 입력하면  
**모두 가능한 날짜와 참여 가능 인원이 많은 날짜를 자동으로 계산해주는 RESTful API 서버**입니다.

단순 일정 조율 기능 구현뿐만 아니라 JPA 조회 성능, 데이터 무결성, Redis TTL을 활용한 방 만료 처리와 장애 상황을 고려한 fallback 구조를 적용했습니다.

---

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

---

## 📌 주요 기능

- **방 생성**: 모임 이름과 일정 후보 날짜 범위를 설정하여 일정 조율방 생성
- **날짜 입력**: 참여자가 자신이 가능한 날짜를 선택하여 등록
- **재입력 지원**: 동일한 이름으로 다시 입력하면 기존 날짜를 새로운 날짜로 갱신
- **최적 날짜 계산**: 전원 참석 가능한 날짜를 우선하고, 이후 참여 가능 인원이 많은 순으로 정렬
- **방 자동 만료**: Redis TTL을 이용해 종료일 기준 활성 상태 자동 만료
- **만료 상태 검증**: DB의 종료일을 최종 기준으로 사용하고 Redis 키 유실 시 활성 상태 복구
- **중복 참여 방지**: 동일한 방에서 동일한 이름의 참여자가 중복 생성되지 않도록 DB 제약조건 적용

---

## 🏗 프로젝트 구조

```text
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

---

## 📡 API 명세

### 방 API

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/rooms` | 방 생성 |
| GET | `/api/rooms/{roomId}` | 방 및 참여자 정보 조회 |

### 참여자 API

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/rooms/{roomId}/availability` | 참여 가능한 날짜 입력 및 재입력 |
| GET | `/api/rooms/{roomId}/result` | 최적 날짜 결과 조회 |

---

## 💡 동작 예시

```text
방 생성
날짜 범위: 6/1 ~ 6/30
        ↓
참여자별 가능한 날짜 입력

이민경: 6/7, 6/14, 6/21
김지수: 6/7, 6/28
철수:   6/7, 6/14
        ↓
최적 날짜 계산

6/7  → 3명 / 전원 가능 ✅
6/14 → 2명
6/21 → 1명
6/28 → 1명
```

전원이 참석할 수 있는 날짜를 우선하고, 이후에는 참석 가능한 인원이 많은 날짜부터 반환합니다.

---

# 🚀 주요 설계 및 개선 사항

## 1. JPA N+1 조회 문제 개선

### 문제

초기 구현에서는 참여자 목록을 조회한 뒤 각 참여자의 가능한 날짜를 반복해서 조회했습니다.

```text
참여자 조회
    ↓
참여자 A → 가능한 날짜 SELECT
참여자 B → 가능한 날짜 SELECT
참여자 C → 가능한 날짜 SELECT
...
```

참여자가 증가할수록 추가 쿼리가 반복되는 N+1 구조였습니다.

### 개선

방에 속한 `AvailableDate`를 한 번에 조회하도록 변경하고, 필요한 `Participant` 정보는 Fetch Join을 이용해 함께 조회하도록 개선했습니다.

```text
Participant 목록 조회
        +
AvailableDate + Participant 일괄 조회
```

조회한 데이터는 애플리케이션에서 참여자 ID를 기준으로 그룹화하여 응답 DTO로 변환합니다.

### 결과

참여자 수에 따라 가능한 날짜 조회 쿼리가 반복 실행되는 구조를 제거하여 조회 성능을 개선했습니다.

---

## 2. 동일 참여자 중복 등록 방지

### 문제

기존에는 애플리케이션에서 참여자의 존재 여부를 확인한 뒤 저장했기 때문에 동시에 동일한 이름으로 최초 등록 요청이 들어오면 중복 데이터가 생성될 가능성이 있었습니다.

```text
요청 A → 참여자 없음 확인
요청 B → 참여자 없음 확인

요청 A → INSERT
요청 B → INSERT
```

### 개선

`participants` 테이블에 `(room_id, name)` 복합 UNIQUE 제약조건을 적용했습니다.

```text
UNIQUE (room_id, name)
```

애플리케이션에서는 `findByRoom_IdAndName()`으로 기존 참여자를 한 번만 조회하고, 존재하는 경우 기존 참여자를 사용하여 가능한 날짜를 갱신합니다.

DB 제약조건 위반은 전역 예외처리를 통해 충돌 응답으로 변환하도록 구성했습니다.

### 결과

- 동일 방에서 동일 이름의 참여자 중복 생성 방지
- 애플리케이션 검증뿐만 아니라 DB 레벨에서도 데이터 무결성 보장
- 불필요한 `exists + find` 중복 조회 제거

---

## 3. 방 생성 날짜 범위 검증

방 생성 시 다음 비즈니스 규칙을 검증합니다.

```text
startDate <= endDate
endDate >= today
```

시작일이 종료일보다 늦거나 이미 종료된 날짜 범위로 방을 생성하는 것을 방지합니다.

날짜 존재 여부와 같은 입력값 검증은 DTO에서 처리하고, 날짜 간 관계와 같은 비즈니스 규칙은 Service 계층에서 처리하도록 역할을 분리했습니다.

---

## 4. Redis TTL 기반 방 만료 관리

방을 생성할 때 종료일까지 남은 기간을 계산하여 Redis에 활성 상태를 저장합니다.

```text
room:{roomId} → active
TTL → endDate 기준 자동 만료
```

종료일 당일까지 방을 사용할 수 있도록 TTL을 계산합니다.

```java
long daysUntilExpiry =
        ChronoUnit.DAYS.between(LocalDate.now(), endDate) + 1;
```

별도의 만료 배치 작업 없이 Redis TTL을 활용하여 활성 상태를 자동으로 정리할 수 있도록 구현했습니다.

---

## 5. Redis 키 유실을 고려한 fallback 처리

### 문제

초기 구현에서는 Redis 키 존재 여부만으로 방의 만료 여부를 판단했습니다.

이 구조에서는 Redis 키가 유실될 경우 DB에 존재하고 아직 종료되지 않은 정상적인 방도 만료된 방으로 판단할 수 있었습니다.

또한 존재하지 않는 방과 실제로 만료된 방을 정확히 구분하기 어려웠습니다.

### 개선

DB를 방 정보의 최종 기준으로 사용하고 Redis를 활성 상태 관리의 보조 저장소로 사용하도록 역할을 분리했습니다.

```text
방 조회
  ↓
DB 존재 여부 확인
  ↓
Redis 활성 상태 확인
  ↓
Redis Key 없음
  ↓
DB endDate 확인
  ├─ 종료됨 → ROOM_EXPIRED
  └─ 활성 상태 → Redis TTL 복구
```

### 결과

- 존재하지 않는 방과 만료된 방을 구분
- Redis 키 유실 시 DB 정보를 기반으로 활성 상태 복구
- Redis 데이터에만 의존하지 않는 방 만료 검증 구조 구성

---

## 6. 일관된 예외 처리

`GlobalExceptionHandler`를 이용하여 애플리케이션 전역의 예외 응답을 일관된 형태로 관리합니다.

주요 예외 상황:

```text
ROOM_NOT_FOUND
ROOM_EXPIRED
INVALID_DATE_RANGE
PARTICIPANT_NOT_FOUND
DUPLICATE_PARTICIPANT
INVALID_INPUT
```

비즈니스 예외와 Bean Validation 오류, DB 제약조건 위반 등을 공통 응답 형식으로 처리합니다.

---

## 7. UUID 기반 방 식별자

일정 조율방은 URL을 통해 여러 사람에게 공유되는 특성이 있기 때문에 순차적인 숫자 ID 대신 UUID 기반 식별자를 사용했습니다.

```text
/api/rooms/{UUID}
```

이를 통해 단순한 순차 ID보다 다른 방의 식별자를 추측하기 어렵도록 구성했습니다.

---

## ⚙️ 실행 방법

### 1. MySQL DB 생성

```sql
CREATE DATABASE meeting_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. Redis 실행

macOS Homebrew 기준:

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

Redis 연결 정보 등 환경별 설정이 필요한 경우 로컬 환경에 맞게 추가합니다.

### 4. 서버 실행

```bash
./gradlew bootRun
```

### 5. Swagger UI

서버 실행 후 브라우저에서 다음 경로로 API 명세를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui.html
```

---

## 🔍 핵심 설계 요약

| 항목 | 적용 내용 |
|---|---|
| 조회 성능 | 방 단위 일괄 조회 및 Fetch Join으로 N+1 개선 |
| 데이터 무결성 | `(room_id, name)` UNIQUE 제약조건 적용 |
| 일정 계산 | 전원 참석 가능 우선 → 참여 가능 인원순 정렬 |
| 날짜 검증 | 시작일·종료일 관계 및 이미 종료된 일정 검증 |
| 만료 관리 | Redis TTL 기반 자동 만료 |
| Redis 안정성 | DB를 최종 기준으로 두고 Redis 키 유실 시 복구 |
| 식별자 | 공유 URL을 고려한 UUID 기반 방 ID |
| 예외 처리 | GlobalExceptionHandler 기반 공통 오류 응답 |

---

## 🎯 프로젝트를 통해 개선한 점

초기 기능 구현 이후 실제 서비스에서 발생할 수 있는 조회 성능과 데이터 정합성 문제를 중심으로 코드를 개선했습니다.

특히 JPA 연관관계 조회 과정에서 발생할 수 있는 N+1 문제를 방 단위 일괄 조회와 Fetch Join으로 개선하고, 동일 참여자의 동시 등록 가능성은 애플리케이션 로직뿐만 아니라 DB UNIQUE 제약조건을 통해 방어했습니다.

또한 Redis TTL에만 의존했던 방 만료 판단을 개선하여 DB를 최종 데이터 기준으로 사용하고 Redis 키가 유실된 경우 DB 정보를 기반으로 활성 상태를 복구하도록 구성했습니다.

이를 통해 단순 기능 구현을 넘어 **조회 성능, 데이터 무결성, 저장소 간 역할 분리와 장애 상황을 고려한 백엔드 설계**를 경험했습니다.