# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 참고할 프로젝트 컨텍스트입니다.

## 프로젝트 개요

- **CUSFIT-BE**: 커스핏 백엔드. Spring Boot 3.5.14, Java 17.
- 현재 스켈레톤 단계입니다 — 도메인 코드(`src/main/java`)는 아직 없고, 인프라/설정만 구성되어 있습니다.

## 기술 스택 (build.gradle, application.yml 기준)

- Spring Boot: Web, Data JPA, Data Redis, OAuth2 Resource Server, Validation, WebSocket, Actuator
- PostgreSQL + PostGIS (`postgis/postgis:16-3.4`) — 위치 기반 데이터를 다룰 것으로 보이는 스키마
- Redis
- Flyway로 스키마 관리. `spring.jpa.hibernate.ddl-auto: validate`이므로 **Hibernate가 스키마를 생성하지 않습니다** — 모든 스키마 변경은 `src/main/resources/db/migration`에 마이그레이션 파일로 작성해야 합니다. (현재 마이그레이션 파일 없음, 폴더에 `.gitkeep`만 있음)
- OAuth2 Resource Server(JWT) — 인증은 외부 Issuer(`OAUTH2_ISSUER_URI`)에 위임하고, 이 서비스는 토큰 검증만 수행합니다. 로그인/발급 로직은 이 저장소에 없습니다.
- AWS S3 (파일 저장), Firebase Admin SDK (FCM 푸시로 추정), springdoc-openapi(Swagger UI: `/swagger-ui.html`, API 문서: `/api-docs`), Micrometer + Prometheus + Loki(로깅/모니터링)
- `open-in-view: false` — 트랜잭션 밖에서 지연 로딩 불가. `docs/CODE_CONVENTIONS.md` 2.4 참고.

## 로컬 실행

1. `.env.example`을 `.env`로 복사하고 값을 채운다 (`DB_*`, `REDIS_*`, `OAUTH2_ISSUER_URI`, `AWS_*`, `FIREBASE_CREDENTIALS_PATH`, `LOKI_URL`).
2. `docker compose up -d` — Postgres(PostGIS) + Redis 컨테이너 기동.
3. `./gradlew bootRun` — `build.gradle`의 `bootRun.doFirst`가 `.env`를 읽어 프로세스 환경변수로 주입한다.

## 테스트

- `./gradlew test` (JUnit 5, `spring.profiles.active=test`)
- 데이터베이스가 필요한 테스트는 Testcontainers(PostgreSQL/PostGIS)를 사용하므로 Docker가 실행 중이어야 합니다. 로컬 개발용 데이터베이스나 `.env`에는 의존하지 않습니다.
- 테스트 작성 규칙(단위/통합 경계, 네이밍, Fixture 등)은 `docs/CODE_CONVENTIONS.md` 4장 참고.

## 코드 컨벤션

- 네이밍, 패키지 배치, 정책 코드 위치, 테스트 규칙은 `docs/CODE_CONVENTIONS.md`에 정리되어 있습니다. 코드를 작성하기 전에 반드시 확인하세요.

## 결정된 아키텍처

### 최상위 패키지 레이아웃: 도메인별 패키지

레이어(controller/service/repository)로 먼저 나누지 않고, 도메인별로 최상위 패키지를 나눕니다. 각 도메인 패키지 안에서 다시 `controller`, `service`(+`impl`), `repository`, `dto`(`request`/`response`), `entity`로 나눕니다. `docs/CODE_CONVENTIONS.md`의 모든 예시(`card/service/...`, `chat/controller/...`)가 이 구조를 전제합니다.

- 예: `com.cusfit.cusfitbe.card`, `com.cusfit.cusfitbe.user`, `com.cusfit.cusfitbe.global`(공통 컴포넌트)

### 환경 프로필 / 스키마 관리

- Spring Profile을 `local`, `test`, `prod` 3개로 나누고 `application-{profile}.yml`로 분리합니다. 공통 설정은 `application.yml`에 두고 프로필별로 다른 값(DB, Redis 호스트, 로그 레벨 등)만 오버라이드합니다.
- Flyway 마이그레이션은 단순 버전 증가(`V1__xxx.sql`, `V2__xxx.sql`, ...) 방식으로 관리합니다. **이미 적용된 마이그레이션 파일은 수정하지 않고, 항상 새 버전 파일을 추가**합니다 (수정하면 체크섬이 깨져 배포 환경에서 Flyway가 실패합니다).

## 아직 정해지지 않은 것 (TBD)

아래는 이 저장소에 아직 실제 코드가 없어 확정되지 않은 항목입니다. 의도적으로 지금 정하지 않고 필요할 때 직접 정하기로 했습니다. 결정되는 즉시 이 파일과 `docs/CODE_CONVENTIONS.md`를 함께 갱신해야 합니다.

- 공통 API 응답 포맷 (예: `ApiResponse<T>` 래퍼 사용 여부/구조) — 추후 직접 정의
- 인증 흐름 세부 (Issuer 종류, JWT 클레임 구조, 소셜 로그인 여부 등) — 추후 직접 정의. `firebase-admin` 의존성이 있지만 FCM 푸시용일 수도, Firebase Auth 겸용일 수도 있어 단정하지 않음.
