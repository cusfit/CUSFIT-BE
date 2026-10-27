# CUSFIT-BE
커스핏 백엔드

## 로컬 실행

Java 17과 Docker가 필요합니다. `.env.example`을 `.env`로 복사해 개발 DB 설정을 채우고 실행합니다.

```sh
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 개발용 저장·조회 API

`local` 프로필에서만 `/api/dev` 아래에 API가 등록됩니다. 인증 없이 사용자 ID를 명시해서 계층 연결을 확인하는 개발용 API입니다. 운영용 인증·권한 계약은 이후 별도로 구현합니다.

각 리소스는 `POST /api/dev/{resource}`(201, Location 헤더)와 `GET /api/dev/{resource}/{id}`(200)를 제공합니다. 성공·실패는 공통 `ApiResponse` 형식으로 반환합니다. 목록·수정·삭제는 아직 제공하지 않습니다.

| 리소스 | 생성 요청 필드 |
|---|---|
| users | nickname |
| terms | type, version, contentUrl, effectiveAt(ISO-8601 UTC) |
| brands | name |
| menus | brandId, name |
| option-presets | brandId, name |
| recipes | authorId, menuId, title, options: [{name}] |
| reports | reporterId, recipeId, content |
| notifications | recipientId, title, content |
| notices | title, content |
| faqs | question, answer |
| inquiries | authorId, title, content |

회원과 브랜드를 먼저 생성하고, 반환된 ID로 메뉴와 레시피를 생성합니다. `options`는 빈 배열도 허용합니다. 모든 생성 요청 필드는 필수입니다. 레시피 옵션은 레시피를 통해서만 저장·조회합니다.

```sh
curl -i http://localhost:8080/api/dev/users \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"커스핏"}'
curl http://localhost:8080/api/dev/users/1
```

DB 스키마는 Flyway로 관리하며 Hibernate는 검증만 합니다. 상세 합의와 제외 범위는 [개발용 도메인 계약](docs/tasks/domain-basics-contract.md)에 정리했습니다.

## 검증

```sh
./gradlew check
```

Repository 테스트는 Testcontainers로 격리된 PostgreSQL/PostGIS를 사용하므로 Docker가 실행 중이어야 합니다. 로컬 DB, 외부 인증, S3, Firebase에 의존하지 않습니다.

iCloud 등 동기화 폴더에서 빌드 산출물이 복제되거나 파일 읽기가 지연되면 산출물 경로를 폴더 밖으로 지정할 수 있습니다. 기본 경로는 기존처럼 `build/`입니다.

```sh
./gradlew check -PcusfitBuildDir=/tmp/cusfit-build
```
