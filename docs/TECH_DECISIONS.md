# 구현 결정 목록

코드를 쓰기 시작하면 기획서나 화면에는 나오지 않지만 누군가는 정해야 하는 것들이 생깁니다. 이 문서는 그런 구현 수준의 결정을 모은 것입니다. 기획 정책(무엇을 할지)은 `docs/BUSINESS_POLICY.md`, 코드 모양(어떻게 쓸지)은 `docs/CODE_CONVENTIONS.md`에 있고, 이 문서는 그 사이에서 DB 설계, 삭제 방식, 동시성, 인증 연동처럼 구현할 때 갈리는 지점을 다룹니다.

항목마다 추천안이 있지만 아직 확정이 아닙니다. 정하면 상태를 `확정`으로 바꾸고, 규칙으로 굳힐 내용은 `CODE_CONVENTIONS.md`로 옮깁니다.

## 기본 저장·조회 구현에서 확정한 항목

이번 구현에서는 아래 항목만 확정했습니다. 아래에 남아 있는 다른 추천안은 계속 미정입니다. 상세 계약은 `docs/tasks/domain-basics-contract.md`를 따릅니다.

- D-01: `Long` / `BIGINT GENERATED ALWAYS AS IDENTITY`.
- D-02: 복수형 snake_case 테이블명, 단수 엔티티명(약관은 `Terms`), snake_case 컬럼명.
- D-03 일부: `Instant` / `timestamptz` 생성 시각. `BaseCreatedEntity`의 `@PrePersist`로 채웁니다. 현재 수정 기능이 없으므로 `updated_at` 및 Auditing 설정은 추가하지 않습니다.
- D-05: 도메인 간 Java 참조는 ID만 사용하고 DB에는 FK를 둡니다. 삭제 API가 없으므로 기본 NO ACTION입니다.
- D-06: 레시피 옵션은 레시피 내부 자식으로 `cascade = ALL`, `orphanRemoval = true`, DB `ON DELETE CASCADE`를 사용합니다.
- API: `local` 전용 `/api/dev` 생성·단건 조회. 운영 인증·권한은 미정이며 개발 API는 명시적 사용자 ID를 받습니다.
- 문자열은 `text`, 필수값은 NOT NULL 및 빈 문자열 검증. 닉네임 중복·길이 제한·삭제 상태·약관 종류 enum 등 제품 정책은 추가하지 않습니다.
- 초기 운영 데이터, 목록·검색·페이지네이션, 외부 연동은 이번 범위에서 제외합니다.

- 🔴 user 도메인 파일럿(첫 엔티티 + Flyway) 전에 정해야 하는 항목
- 🟡 해당 도메인 엔티티 작업 전에 정하면 되는 항목
- ⚪ Service·Controller 단계에서 정해도 되는 항목

---

## 0. 지금 저장소 상태와 어긋나는 것

| # | 내용 | 근거 | 추천 |
|---|---|---|---|
| 0-1 | 인증 구조가 정해지지 않았는데 리소스 서버 설정만 있음 | `CLAUDE.md` TBD, `application.yml`의 `issuer-uri` | 3장 A-01에서 결정 🔴 |
| 0-2 | 위치 기능이 없는데 PostGIS 이미지와 `hibernate-spatial` 의존성이 있음 | `docker-compose.yml`, `CusfitBeApplicationTests`, `build.gradle` | 일반 `postgres:16`으로 바꾸고 `hibernate-spatial` 제거 🟡 |
| 0-3 | 실시간 기능이 없는데 `spring-boot-starter-websocket` 의존성이 있음 | `build.gradle` | 제거 ⚪ |
| 0-4 | 컨벤션 예시가 UUID ID를 전제함 (Deoham-BE 예시) | `CODE_CONVENTIONS.md` 1.6, 7.3 | 1장 D-01 결정 후 예시 갱신 🔴 |
| 0-5 | 실패 응답에 데이터를 실을 수 없음 | `ApiResponse.fail()`은 `data`가 항상 null | 5장 API-03에서 결정 ⚪ |

---

## 1. DB 기본 설계

### D-01. PK 타입 🔴

- 선택지
  - A. `BIGINT GENERATED ALWAYS AS IDENTITY` (`Long`)
  - B. UUID v4 (랜덤)
  - C. UUID v7 (시간순)
- 고려할 점
  - UUID v4는 B-tree 인덱스에 무작위로 들어가 쓰기와 인덱스 크기에서 불리합니다.
  - 숫자 ID를 API에 그대로 내보내면 `/recipes/1`, `/recipes/2`처럼 순서대로 긁어 갈 수 있습니다. 하지만 커스핏에서 외부에 노출되는 건 레시피 공유 URL뿐이고, 여기에는 별도의 랜덤 슬러그를 쓰기로 했습니다(BUSINESS_POLICY R-09).
  - Spring Boot 3.5의 Hibernate 6.6에서 UUID v7을 기본 기능으로 만들 수 있는지는 확인이 필요합니다. 안 되면 생성기를 직접 써야 합니다.
- 추천: A. 가장 단순하고 인덱스가 작습니다. 공개 URL은 슬러그로 해결합니다. 정하면 컨벤션의 UUID 예시를 `Long`으로 바꿉니다.

### D-02. 테이블·컬럼 이름 🔴

- 결정할 것: 테이블 이름을 단수로 할지 복수로 할지.
- 주의: PostgreSQL에서 `user`는 예약어라 테이블 이름으로 쓰면 매번 따옴표로 감싸야 합니다.
- 추천: 테이블은 복수형 snake_case(`users`, `recipes`, `recipe_options`), 컬럼은 snake_case. 엔티티 이름은 단수(`User`)로 두고 `@Table(name = "users")`를 명시합니다. Spring Boot 기본 네이밍 전략이 camelCase를 snake_case로 바꿔 주므로 컬럼에는 `@Column(name)`을 따로 쓰지 않습니다.

### D-03. 공통 컬럼과 시간 타입 🔴

- 결정할 것: 모든 테이블에 넣을 컬럼, 시간을 담는 자바 타입과 DB 타입, 서버 시간대.
- 추천
  - 모든 테이블에 `created_at`, 변경되는 테이블에는 `updated_at`을 둡니다. `BaseTimeEntity`(`@MappedSuperclass`)와 JPA Auditing으로 채웁니다.
  - 자바는 `Instant`, DB는 `timestamptz`를 씁니다. `LocalDateTime`은 시간대 정보가 없어 서버 시간대가 바뀌면 값의 의미가 바뀝니다.
  - JVM과 DB 세션 시간대는 UTC로 고정하고, API는 ISO-8601 UTC 문자열로 주고받습니다. "5분 전", "7/1" 같은 표시는 앱이 한국 시간으로 바꿉니다.
  - "하루 20회"처럼 날짜 경계가 필요한 계산은 한국 시간 기준으로 하고 그 위치를 코드에 한 곳으로 모읍니다.

### D-04. enum 저장 방식 🔴

- 선택지
  - A. `@Enumerated(EnumType.STRING)` + `varchar` + `CHECK` 제약
  - B. PostgreSQL `ENUM` 타입
  - C. `@Enumerated(EnumType.ORDINAL)` (숫자)
- 추천: A. B는 값을 추가할 때 `ALTER TYPE`이 필요하고 Hibernate 매핑도 번거롭습니다. C는 enum 순서를 바꾸면 데이터가 깨집니다. `CHECK` 제약은 값이 늘어날 때 새 마이그레이션으로 교체합니다.

### D-05. 외래 키(FK) 제약 🟡

- 결정할 것: 도메인 사이는 엔티티 대신 ID로만 참조하기로 했는데(컨벤션 7.3), DB에는 FK 제약을 걸지.
- 선택지
  - A. 모든 참조에 FK 제약, 삭제 시 `RESTRICT`(기본값)
  - B. 같은 도메인 안에만 FK, 도메인 사이는 FK 없음
- 고려할 점
  - FK가 있으면 존재하지 않는 사용자의 레시피 같은 데이터가 생길 수 없습니다.
  - FK가 있으면 삭제 순서가 중요해집니다. 탈퇴 정리를 이벤트로 나눠 처리할 때 순서가 어긋나면 실패합니다.
  - 탈퇴한 사용자 행은 지우지 않고 남기기로 추천했기 때문에(BUSINESS_POLICY C-02) `users`를 가리키는 FK는 깨질 일이 거의 없습니다.
- 추천: A. 단, 30일 뒤 레시피를 물리 삭제할 때 신고 행이 그 레시피를 가리키고 있으면 FK 때문에 지울 수 없습니다. 그래서 `reports.recipe_id`는 NULL 허용으로 두고, 신고에는 처리에 필요한 최소 정보(레시피 제목 등)를 따로 복사해 둡니다.

### D-06. Aggregate 내부 자식의 생명주기 🟡

- 대상: `Recipe` → `RecipeOption`, `Inquiry` → `InquiryImage`처럼 부모 없이 의미 없는 자식.
- 추천: JPA에서는 `cascade = ALL` + `orphanRemoval = true`, DB에서는 자식 FK에 `ON DELETE CASCADE`를 같이 겁니다. 배치로 부모를 대량 삭제할 때 JPA를 거치지 않아도 자식이 함께 지워집니다. 다른 Aggregate로는 cascade를 걸지 않습니다.

---

## 2. 삭제

### DEL-01. 테이블별 soft delete / hard delete 🔴

기획 정책(BUSINESS_POLICY C-01, U-06)을 테이블 단위로 옮긴 표입니다.

| 테이블 | 방식 | 비고 |
|---|---|---|
| `users` | 지우지 않음. 탈퇴 시 `status = WITHDRAWN`, 개인정보 컬럼 NULL | 다른 테이블의 FK가 계속 유효 |
| `social_accounts` | 탈퇴 시 hard delete | 같은 소셜 계정으로 재가입하면 새 회원 |
| `term_agreements` | 탈퇴 시 hard delete | 법무 확인 후 바뀔 수 있음 |
| `recipes` | soft delete (`status = DELETED`, `deleted_at`), 30일 뒤 배치로 hard delete | 신고 처리 기간 확보 |
| `recipe_options` | 부모를 따라감 (D-06) | |
| `recipe_likes`, `recipe_bookmarks` | hard delete | 레시피 삭제 때 같은 트랜잭션에서 삭제 |
| `ai_feedbacks` | 지우지 않음. 탈퇴 시 `user_id`만 NULL | 품질 데이터 |
| `reports` | 지우지 않음. 신고자 탈퇴 시 `reporter_id` NULL | |
| `notifications` | hard delete. 90일 지난 것은 배치로 삭제 | |
| `device_tokens` | hard delete | 로그아웃·탈퇴·무효 토큰 |
| `inquiries`, `inquiry_images` | 탈퇴 시 hard delete. S3 파일도 삭제 | |
| `brands`, `menus`, `option_presets` | 지우지 않음. `active = false` | 레시피가 참조 중 |

### DEL-02. soft delete 구현 방식 🟡

- 선택지
  - A. Hibernate `@SQLRestriction("deleted_at IS NULL")`을 엔티티에 붙여 모든 조회에서 자동으로 걸러냄
  - B. 자동 필터 없이 리포지토리 메서드마다 상태 조건을 명시
- 고려할 점
  - A는 편하지만, 지워진 것도 봐야 하는 조회(운영자의 신고 검토, 배치 정리)에서 필터를 피하기 어렵고, 네이티브 쿼리에는 적용되지 않아 일관성이 깨집니다.
  - 레시피는 이미 `BLINDED` 상태가 있어서 "보이는 레시피" 조건이 `status = ACTIVE` 하나로 정리됩니다.
- 추천: B. `recipes.status`를 `ACTIVE`, `BLINDED`, `DELETED`로 두고, 목록 조회는 `findBy...AndStatus(ACTIVE)`처럼 명시합니다.
- 함께 챙길 것: soft delete 테이블의 유니크 제약은 부분 인덱스로 만듭니다. 예: 중복 레시피 시그니처는 `CREATE UNIQUE INDEX ... ON recipes (author_id, option_signature) WHERE status <> 'DELETED'`. 그렇지 않으면 지운 레시피와 같은 조합을 다시 만들 수 없습니다.

### DEL-03. 대량 삭제와 카운트 보정 🟡

- 상황: 탈퇴한 사용자가 좋아요 300개를 눌렀다면 좋아요 행 300개를 지우고, 300개 레시피의 `like_count`를 각각 1씩 줄여야 합니다.
- 추천
  - JPA로 하나씩 지우지 않고 벌크 쿼리를 씁니다. 먼저 `UPDATE recipes SET like_count = like_count - 1 WHERE id IN (SELECT recipe_id FROM recipe_likes WHERE user_id = ?)`, 그다음 `DELETE FROM recipe_likes WHERE user_id = ?`.
  - 벌크 쿼리는 영속성 컨텍스트를 거치지 않으므로 `@Modifying(clearAutomatically = true)`로 1차 캐시를 비웁니다.
  - 카운트가 음수가 되지 않도록 `CHECK (like_count >= 0)`을 겁니다.

### DEL-04. S3 파일 정리 ⚪

- 상황: DB 트랜잭션은 롤백되는데 S3 파일은 이미 지워졌거나, 반대로 DB는 지웠는데 S3 삭제가 실패할 수 있습니다.
- 추천
  - S3 삭제는 커밋 이후(`@TransactionalEventListener(AFTER_COMMIT)`)에 합니다.
  - 실패해도 사용자 요청은 성공으로 끝내고 로그를 남깁니다. DB에 없는 S3 파일을 주기적으로 지우는 정리 배치를 둡니다.
  - 지금 S3에 올라가는 건 1:1 문의 이미지뿐입니다(프로필 이미지 없음, 촬영 이미지는 저장하지 않음). 업로드는 서버로 multipart를 받아 올리는 방식으로 충분합니다.

---

## 3. 인증과 사용자 식별

### A-01. 토큰 발급 주체 🔴

- 상황: `CLAUDE.md`는 "외부 Issuer가 발급하고 이 서비스는 검증만 한다"고 적었지만 Issuer가 무엇인지는 정해지지 않았습니다. Apple·구글 로그인이 주는 ID 토큰은 유효 시간이 짧고 갱신 방법이 없어서 그대로 API 토큰으로 쓸 수 없습니다. 누군가는 우리 서비스용 토큰을 발급해야 합니다.
- 선택지
  - A. Firebase Authentication을 Issuer로 사용. 앱이 Firebase로 Apple·구글 로그인을 하고 Firebase ID 토큰을 보내면, 서버는 리소스 서버로 검증만 합니다(`issuer-uri: https://securetoken.google.com/<project-id>`).
  - B. 서버가 Apple·구글 토큰을 직접 검증하고 자체 JWT(access + refresh)를 발급. 이전 프로젝트(Deoham-BE)의 카카오 로그인 방식입니다.
- 고려할 점
  - A는 지금 설정(리소스 서버 + `firebase-admin`)과 그대로 맞고, 토큰 갱신·폐기를 Firebase가 처리합니다. 대신 Firebase에 의존합니다.
  - B는 refresh 토큰 저장(Redis), 로그아웃 시 폐기, 키 관리까지 직접 해야 합니다.
- 추천: A. 정하면 `CLAUDE.md`의 TBD를 갱신합니다. 탈퇴 시 Firebase 사용자 삭제(`FirebaseAuth.deleteUser`)와 Apple 토큰 철회도 같이 처리해야 합니다. Apple 토큰 철회를 Firebase로 할 수 있는지는 확인이 필요합니다.

### A-02. 토큰과 내부 사용자의 연결 🔴

- 결정할 것: 토큰의 `sub`를 `users.id`로 바꾸는 방식과, 토큰은 유효한데 아직 회원가입(닉네임·약관)을 안 한 상태의 처리.
- 추천
  - `users`에 `auth_uid`(Firebase UID 또는 소셜 ID) 유니크 컬럼을 둡니다. 요청마다 `auth_uid`로 사용자를 찾아 `@AuthenticationPrincipal`이나 커스텀 ArgumentResolver로 `userId`를 넘겨줍니다.
  - 가입 전 상태를 위해 `users.status`에 `PENDING`(토큰은 있으나 닉네임·약관 미완료)을 둘지, 가입 완료 시점에 처음 행을 만들지 정해야 합니다. 추천은 후자입니다. 가입 완료 API만 "사용자 없음"을 허용하고, 나머지 API는 사용자가 없으면 `UNAUTHORIZED`를 반환합니다.

### A-03. 탈퇴 후 남은 토큰 🟡

- 상황: 탈퇴해도 이미 발급된 access 토큰은 만료 전까지 유효합니다.
- 추천: A-02에서 요청마다 사용자를 조회하므로, 그때 `status = WITHDRAWN`이면 거부합니다. 요청마다 DB 조회가 한 번씩 늘지만 MVP 규모에서는 문제가 되지 않습니다. 부담이 되면 Redis 캐시를 붙입니다.

---

## 4. 동시성과 정합성

### CON-01. 카운터 컬럼 덮어쓰기 🔴 (recipe 전에 반드시)

- 상황: 좋아요·저장 카운트는 `UPDATE ... SET like_count = like_count + 1`로 원자적으로 올리기로 했습니다. 그런데 작성자가 레시피를 수정하는 트랜잭션에서 엔티티를 먼저 읽어 둔 상태라면, JPA 변경 감지가 UPDATE할 때 모든 컬럼을 쓰면서 읽을 당시의 오래된 `like_count`로 덮어씁니다. 그 사이 눌린 좋아요가 사라집니다.
- 추천: 엔티티의 카운터 필드에 `@Column(insertable = false, updatable = false)`를 붙여 JPA가 이 컬럼을 절대 쓰지 않게 합니다. 카운터 변경은 리포지토리의 원자적 UPDATE로만 합니다. 이 규칙은 컨벤션에 추가할 만합니다.

### CON-02. 유니크 제약으로 중복 막기 🟡

- 대상: 닉네임 중복, 좋아요·저장 중복(같은 사용자, 같은 레시피), 중복 레시피, 중복 신고.
- 상황: "있는지 조회 → 없으면 저장"은 동시 요청 두 개가 둘 다 "없음"을 보고 둘 다 저장할 수 있습니다.
- 추천: 최종 방어는 DB 유니크 제약이 하고, 애플리케이션은 `DataIntegrityViolationException`을 잡아 `CONFLICT`로 바꿉니다. 사전 조회는 친절한 에러 메시지를 위한 용도로만 씁니다. 닉네임은 대소문자 무시 중복이므로 `UNIQUE (lower(nickname))` 함수 인덱스를 씁니다.

### CON-03. 좋아요·저장 API의 멱등성 ⚪

- 선택지
  - A. `POST /recipes/{id}/like` 토글
  - B. `PUT /recipes/{id}/like`(누르기) + `DELETE /recipes/{id}/like`(취소)
- 추천: B. 네트워크 재시도로 같은 요청이 두 번 와도 결과가 같습니다. 이미 눌렀는데 또 PUT이 오면 에러 없이 성공을 반환하고 카운트는 올리지 않습니다.

### CON-04. 낙관적 락(`@Version`) ⚪

- 추천: MVP에서는 쓰지 않습니다. 레시피는 작성자 한 명만 수정하고, 경쟁이 있는 카운터는 원자적 UPDATE로 처리합니다. 운영자 여러 명이 같은 신고를 동시에 처리하는 상황이 생기면 `reports`에 추가합니다.

### CON-05. 외부 API 호출과 트랜잭션 ⚪

- 상황: AI 분석은 수 초가 걸립니다. DB 트랜잭션 안에서 호출하면 그동안 커넥션을 붙잡고 있어 커넥션 풀(기본 10개)이 금방 바닥납니다.
- 추천: AI 분석 API는 DB 트랜잭션 없이 실행합니다. 결과는 저장하지 않고 응답으로만 돌려주고, 사용자가 "레시피 저장하기"를 누를 때 별도 트랜잭션으로 저장합니다. 분석 횟수 제한은 Redis `INCR` + TTL로 셉니다.

### CON-06. 마일스톤 알림 판정 🟡

- 상황: "저장 100번" 알림을 보내려면 카운트를 올린 직후의 값을 알아야 합니다. 올린 뒤 다시 SELECT하면 그 사이 다른 요청이 끼어들어 100을 건너뛰거나 두 번 보낼 수 있습니다.
- 추천: 네이티브 쿼리 `UPDATE recipes SET save_count = save_count + 1 WHERE id = ? RETURNING save_count`로 올린 값을 바로 받습니다. 그 값이 기준에 걸리면 알림 이벤트를 발행합니다. 마지막으로 알린 기준을 레시피에 저장해 취소 후 재저장으로 같은 알림이 다시 나가지 않게 합니다.

---

## 5. 조회와 API

### API-01. 페이지네이션 🟡

- 선택지
  - A. offset (`page`, `size`)
  - B. 커서 (`마지막 항목의 정렬 키`로 다음 페이지)
- 고려할 점: 무한 스크롤에서 offset은 그 사이 새 글이 생기면 같은 항목이 두 번 보입니다. 인기순은 정렬 키(저장 수)가 계속 바뀌어서 커서도 완벽하지 않습니다.
- 추천: 최신순 목록과 알림은 커서(`created_at`, `id`), 인기순은 offset으로 시작합니다. 응답에는 전체 개수 대신 `hasNext`만 줍니다(`Slice`). 전체 개수 COUNT 쿼리가 필요 없어집니다. 단, 저장한 레시피 화면처럼 개수를 보여 주는 곳은 따로 개수를 조회합니다.

### API-02. 검색 구현 🟡

- 상황: 검색창 "브랜드·메뉴·레시피·작성자 검색"은 부분 일치 검색입니다. `LIKE '%자바칩%'`은 일반 인덱스를 못 탑니다. PostgreSQL 기본 전문 검색은 한국어 형태소 분석기가 없습니다.
- 추천: `pg_trgm` 확장과 GIN 인덱스를 씁니다. 이 확장은 공식 PostgreSQL 이미지에 포함되어 있어 Flyway에서 `CREATE EXTENSION IF NOT EXISTS pg_trgm`으로 켤 수 있습니다. 검색 대상이 늘거나 느려지면 그때 전용 검색 엔진을 검토합니다.

### API-03. 클라이언트가 분기해야 하는 실패 응답 ⚪

- 상황
  - 중복 레시피 경고는 "기존 레시피 보기" 버튼 때문에 기존 레시피 ID가 필요합니다. 그런데 `ApiResponse.fail()`은 `data`를 항상 비워서 보냅니다.
  - 하루 횟수 초과는 HTTP 429가 맞는데 `ErrorCode`에 429가 없습니다.
- 선택지
  - A. 중복을 실패가 아닌 성공 응답의 한 형태로 표현(`{ "duplicated": true, "existingRecipeId": 12 }`)
  - B. `ErrorBody`에 상세 데이터 필드를 추가
- 추천: 중복 레시피는 A. 저장 전에 "중복 확인" API를 따로 두고 그 결과로 경고를 띄우면 실패 응답에 데이터를 실을 필요가 없습니다. 저장 API는 최후 방어로 `CONFLICT`만 반환합니다. 429는 `TOO_MANY_REQUESTS` 코드를 추가합니다(컨벤션 2.3의 "클라이언트가 분기해야 하는 계약").

### API-04. 목록 화면의 다른 도메인 데이터 조합 🟡

- 상황: 레시피 카드에는 브랜드명·메뉴명(catalog), 작성자 닉네임(user)이 필요합니다. 도메인 사이는 ID로만 참조하므로 JOIN 대신 조합이 필요합니다.
- 선택지
  - A. 레시피 목록을 조회한 뒤, 거기서 모은 ID로 catalog·user의 ReadService를 `IN` 조회로 한 번씩 호출해 조합. 페이지당 쿼리 3개.
  - B. recipe 도메인에 다른 도메인 테이블까지 JOIN하는 읽기 전용 쿼리를 둠.
- 추천: A. ArchUnit 규칙을 지키고, 쿼리 수가 페이지 크기와 무관하게 고정됩니다. 느려지면 B를 예외로 허용하고 이유를 남깁니다.

### API-05. 길이·형식 제한의 단일 출처 ⚪

- 상황: 닉네임 2~12자 같은 규칙이 요청 DTO(`@Size`), 엔티티 불변식, DB 컬럼 길이 세 곳에 따로 적히면 하나만 바뀌는 일이 생깁니다.
- 추천: 엔티티나 값 객체(`Nickname`)에 상수로 두고, DTO 검증과 엔티티 검증이 그 상수를 참조합니다. DB 컬럼 길이는 마이그레이션에 같은 값을 쓰고 Repository 테스트로 확인합니다.

---

## 6. 데이터와 운영

### OPS-01. 카탈로그 초기 데이터 🟡

- 선택지
  - A. Flyway 버전 마이그레이션(`V__`)에 INSERT로 넣음
  - B. Flyway 반복 마이그레이션(`R__`)
  - C. 관리자 API로만 넣음
- 추천: 최초 브랜드·메뉴·옵션은 A로 넣고, 이후 변경은 관리자 API로 합니다. B는 파일이 바뀔 때마다 다시 실행되어 운영자가 바꾼 데이터를 덮어쓸 수 있습니다. 테스트 컨테이너에도 같은 초기 데이터가 들어가므로 테스트는 이 데이터를 전제로 하지 않고 Fixture로 필요한 데이터를 직접 만듭니다.

### OPS-02. JSON 데이터 저장 ⚪

- 대상: AI가 처음 낸 결과(BUSINESS_POLICY R-11).
- 추천: `jsonb` 컬럼에 저장하고, 엔티티에서는 `@JdbcTypeCode(SqlTypes.JSON)`으로 매핑합니다. 내용으로 검색할 일이 생기기 전에는 인덱스를 걸지 않습니다.

### OPS-03. 약관 본문 저장 위치 ⚪

- 추천: 약관 본문은 정적 페이지(S3 또는 웹)에 두고 DB에는 `종류, 버전, 시행일, URL`만 저장합니다. 본문을 DB에 넣으면 수정할 때마다 마이그레이션이나 관리자 화면이 필요합니다.

### OPS-04. 스케줄러와 서버 대수 ⚪

- 상황: 레시피 30일 뒤 삭제, 알림 90일 뒤 삭제, S3 정리 같은 배치가 생깁니다. 서버를 2대 이상 띄우면 같은 배치가 동시에 돕니다.
- 추천: MVP는 서버 1대를 전제로 하고 그 사실을 문서에 적습니다. 배치는 다시 실행해도 안전하게(멱등) 만듭니다. 서버를 늘릴 때 ShedLock 같은 분산 락을 붙입니다.

### OPS-05. 로그에 개인정보 남기지 않기 ⚪

- 추천: 이메일, 닉네임, 토큰, 문의 본문은 로그에 남기지 않고 `userId`만 남깁니다. Loki로 로그가 외부에 모이므로 탈퇴 후에도 로그에 개인정보가 남는 문제를 막습니다. 요청 로깅 필터를 만들 때 바디를 통째로 찍지 않습니다.

---

## 7. 결정 순서

| 순서 | 항목 | 이유 |
|---|---|---|
| 1 | A-01, A-02 | `users` 테이블의 식별 컬럼과 가입 흐름이 이것에 달려 있습니다. |
| 2 | D-01, D-02, D-03, D-04 | 첫 마이그레이션(`V1`)에 들어갈 공통 규칙입니다. 나중에 바꾸면 모든 테이블을 고쳐야 합니다. |
| 3 | DEL-01 | `users.status`, 개인정보 컬럼 NULL 허용 |
| 4 | 0-2 | PostGIS를 뺄지 첫 마이그레이션 전에 정하면 테스트 이미지도 같이 바꿀 수 있습니다. |
| 5 | CON-01, CON-02, DEL-02, D-05, D-06 | recipe 엔티티 전 |
| 나머지 | ⚪ 항목 | Service·Controller 단계 |
