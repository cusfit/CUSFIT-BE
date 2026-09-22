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
- Testcontainers(PostgreSQL) 의존성이 이미 포함되어 있음.
- 테스트 작성 규칙(단위/통합 경계, 네이밍, Fixture 등)은 `docs/CODE_CONVENTIONS.md` 4장 참고.

## 코드 컨벤션

- 네이밍, 패키지 배치, 정책 코드 위치, 테스트 규칙은 `docs/CODE_CONVENTIONS.md`에 정리되어 있습니다. 코드를 작성하기 전에 반드시 확인하세요.

## 아직 정해지지 않은 것 (TBD)

아래는 `docs/CODE_CONVENTIONS.md`가 원래 참조하려 했던 상위 아키텍처 항목인데, 이 저장소에 아직 실제 코드가 없어 확정되지 않았습니다. 결정되는 즉시 이 파일과 `docs/CODE_CONVENTIONS.md`를 함께 갱신해야 합니다.

- 공통 API 응답 포맷 (예: `ApiResponse<T>` 래퍼 사용 여부/구조)
- 인증 흐름 세부 (Issuer 종류, JWT 클레임 구조, 소셜 로그인 여부 등)
- 프로필/스키마 관리 방식
- 최상위 패키지 레이아웃 (도메인별 패키지 분리 여부 — `docs/CODE_CONVENTIONS.md`의 예시는 도메인별 패키지를 전제하지만 이 저장소에서 확정된 것은 아님)
