# CUSFIT API 명세서 (초안 v0.1)

2026년 11월 5일 · @Avocado

## 읽는 법

이 문서는 Figma 와이어프레임 v2 화면과 cusfit/CUSFIT-BE(develop, 커밋 2797000) 코드를 근거로 만든 API 명세 초안입니다. 피드백을 받은 뒤 확정본으로 고칩니다.

모든 항목에 출처 표시를 붙였습니다. 피드백할 때는 **[추정]**과 **[정책 추천]**부터 보면 됩니다.

| 표시 | 뜻 | 확정 여부 |
|---|---|---|
| [도메인] | 저장소 코드(엔티티, DTO, Flyway 스키마, ErrorCode, ApiResponse)에 이미 있는 내용 | 코드 기준 사실 |
| [Figma] | 화면에 보이는 입력란, 버튼, 문구, 글자 수 표시에서 가져온 내용 | 화면 기준 사실, API 형태는 아님 |
| [정책 추천] | 저장소 `docs/BUSINESS_POLICY.md`, `docs/TECH_DECISIONS.md`에 팀이 적어 둔 추천안. 문서 스스로 "아직 확정이 아닙니다"라고 적혀 있음 | 미확정 |
| [추정] | 위 자료에 없어서 제가 정한 값 (URL, 필드명, 페이지 크기 등) | 미확정 |

근거 자료

- Figma 파일 커스핏, 섹션 와이어프레임 v2(node 6:64), 화면 약 50개
- 저장소 코드: `src/main/java/com/cusfit/cusfitbe/**`, `src/main/resources/db/migration/V1~V7`
- 저장소 문서: `CLAUDE.md`, `docs/BUSINESS_POLICY.md`, `docs/TECH_DECISIONS.md`, `docs/CODE_CONVENTIONS.md`

현재 코드에는 local 프로필 전용 개발 API(`/api/dev/**`, 생성과 단건 조회만)만 있습니다 [도메인]. 아래 운영 API는 전부 새로 설계한 것이고, 엔드포인트 URL과 메서드는 별도 표시가 없어도 모두 **[추정]**입니다.

## 공통 규약

응답 포맷과 에러 코드는 코드에 있는 그대로 씁니다. 인증 방식과 페이지네이션은 아직 정해지지 않았습니다.

### 기본 주소

- 운영 API 접두사: `/api/v1` [추정]. 현재 코드는 개발용 `/api/dev`만 있습니다 [도메인].
- 시간은 ISO-8601 UTC 문자열(`2026-11-05T05:00:00Z`)입니다. 코드가 `Instant`/`timestamptz`를 씁니다 [도메인].
- ID는 숫자(Long)입니다 [도메인].

### 응답 포맷 [도메인]

모든 응답은 `ApiResponse<T>`로 감쌉니다(`global/response/ApiResponse.java`).

```json
// 성공
{ "success": true, "data": { ... }, "error": null }

// 실패
{ "success": false, "data": null, "error": { "code": "NOT_FOUND", "message": "Recipe not found" } }
```

- 실패 때 `data`는 항상 `null`입니다. 그래서 "중복 레시피의 기존 ID" 같은 값은 실패 응답으로 돌려줄 수 없습니다. 이 문서는 중복 확인을 별도 성공 응답 API로 두었습니다 [정책 추천: API-03].
- `message`는 표시용이고, 클라이언트는 `code`로만 분기합니다 [도메인: CODE_CONVENTIONS 2.3].

### 에러 코드

| code | HTTP | 출처 | 쓰임 |
|---|---|---|---|
| INVALID_REQUEST | 400 | [도메인] | 입력 검증 실패, 빈 문자열, 필수값 누락 |
| UNAUTHORIZED | 401 | [도메인] | 토큰 없음·만료, 가입 전 사용자 |
| FORBIDDEN | 403 | [도메인] | 남의 레시피 수정·삭제, 본인 레시피 좋아요·저장·신고 |
| NOT_FOUND | 404 | [도메인] | 없는 리소스, 삭제된 레시피 |
| CONFLICT | 409 | [도메인] | 닉네임 중복, 레시피 중복, 중복 신고 |
| INTERNAL_ERROR | 500 | [도메인] | 처리되지 않은 예외 |
| TOO_MANY_REQUESTS | 429 | [정책 추천: R-03, API-03] | AI 분석·레시피 생성 하루 횟수 초과. 아직 enum에 없음 |
| RECIPE_BLINDED | 404 또는 410 | [추정] | 블라인드된 레시피. 삭제 안내와 다른 화면을 띄우려면 별도 코드가 필요합니다(R-09) |
| AI_ANALYSIS_FAILED | 422 | [추정] | 이미지에서 메뉴를 읽지 못한 경우. Figma에 실패 화면은 없음 |

### 인증

- `Authorization: Bearer <JWT>` 헤더로 보냅니다. 서버는 OAuth2 Resource Server로 토큰 검증만 합니다 [도메인: build.gradle, CLAUDE.md].
- 토큰 발급자(Issuer)는 미정입니다. Firebase Authentication을 쓰는 안이 추천되어 있습니다 [정책 추천: A-01]. 이 안이면 로그인·토큰 갱신 API는 서버에 필요 없습니다.
- 각 API의 권한은 아래 표의 인증 열에 적었습니다. 필수 = 회원만, 선택 = 비로그인도 가능(로그인 시 좋아요 여부 등 추가), 없음 = 누구나, 관리자 = `ROLE_ADMIN`.
- 비로그인 둘러보기는 Figma 로그인 화면의 "로그인 없이 둘러보기"와 "로그인 유도" 바텀시트에 근거합니다 [Figma]. 어느 API까지 비로그인을 허용할지는 **[추정]**입니다.

### 페이지네이션 [정책 추천: API-01]

- 최신순 목록과 알림: 커서 방식. 요청 `?cursor=<문자열>&size=20`, 응답 `{ "items": [...], "nextCursor": "...", "hasNext": true }`. 파라미터명과 기본 크기 20은 [추정].
- 인기순: offset 방식. 요청 `?page=0&size=20`, 응답 `{ "items": [...], "hasNext": true }`.
- 전체 개수(`totalCount`)는 주지 않고, 개수가 화면에 보이는 곳만 별도로 줍니다.

## 회원·약관 (user, terms)

현재 `users` 테이블에는 `id`, `nickname`, `created_at`만 있습니다 [도메인]. 이메일, 소셜 계정 연결, 상태, 약관 동의 이력은 스키마에 없어서 아래 API를 만들려면 추가해야 합니다.

| | 메서드 | 경로 | 인증 | 화면 |
|---|---|---|---|---|
| U1 | GET | `/api/v1/users/nickname/availability` | 토큰만 | 회원가입 |
| U2 | POST | `/api/v1/users` | 토큰만 | 회원가입 |
| U3 | GET | `/api/v1/users/me` | 필수 | 마이페이지 |
| U4 | PATCH | `/api/v1/users/me` | 필수 | 프로필 수정 |
| U5 | DELETE | `/api/v1/users/me` | 필수 | 회원 탈퇴 |
| T1 | GET | `/api/v1/terms` | 없음 | 회원가입 |
| T2 | GET | `/api/v1/terms/{termsId}` | 없음 | 약관 상세, 개인정보처리방침 상세 |

"토큰만"은 소셜 로그인 토큰은 있지만 아직 가입하지 않은 상태를 뜻합니다. 가입 완료 시점에 `users` 행을 처음 만드는 안을 따랐습니다 [정책 추천: A-02].

### U1. 닉네임 사용 가능 여부

`GET /api/v1/users/nickname/availability?nickname=온도`

| 응답 필드 | 타입 | 설명 | 출처 |
|---|---|---|---|
| available | boolean | 사용 가능 여부 | [Figma] "사용할 수 있는 닉네임이에요" / "이미 사용 중인 닉네임이에요" |

- 형식이 틀리면(길이, 문자 종류) 400 `INVALID_REQUEST`. 중복은 에러가 아니라 `available: false`로 줍니다 [추정].
- 대체 닉네임 제안(`suggestions`)은 정책 문서 U-02가 디자인페이지 근거로 언급하지만, 와이어프레임 v2에는 없어 넣지 않았습니다.

### U2. 회원가입

| 요청 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| nickname | string | O | 공백 불가 | [도메인] `@NotBlank` |
| | | | 2~12자, 한글·영문·숫자 | [Figma] 회원가입 안내 문구. 코드에는 아직 없음 |
| | | | 대소문자 구분 없이 중복 불가 | [정책 추천: U-02] |
| agreedTermsIds | number[] | O | 필수 약관 ID가 모두 포함되어야 함 | [추정] 필수 약관 3개는 [Figma] |

- "만 14세 이상"을 약관 항목(`terms.type = AGE_14`)으로 다룹니다 [추정]. 별도 필드(`isOver14`)로 받을 수도 있습니다.
- 이메일은 요청으로 받지 않고 토큰에서 읽습니다 [추정].
- 응답: 201, `data` = U3와 같은 사용자 객체.
- 에러: 닉네임 중복 409 `CONFLICT`, 필수 약관 누락 400 `INVALID_REQUEST`, 이미 가입한 토큰 409 `CONFLICT` [추정].

### U3. 내 정보

| 응답 필드 | 타입 | 출처 |
|---|---|---|
| id | number | [도메인] UserResponse |
| nickname | string | [도메인] |
| createdAt | string(datetime) | [도메인] |
| email | string \| null | [Figma] 마이페이지에 이메일 표시. 스키마에 없음 |
| myRecipeCount | number | [추정] 마이페이지의 숫자 "12"가 내 레시피 수라고 보고 넣음 |

가입하지 않은 토큰이면 401 `UNAUTHORIZED`. 앱은 이 응답으로 회원가입 화면 이동을 판단합니다 [정책 추천: A-02]. 클라이언트 분기가 필요하므로 `USER_NOT_REGISTERED` 같은 별도 코드가 나을 수 있습니다 [추정].

### U4. 프로필 수정

- 요청: `{ "nickname": "현영" }`. 규칙은 U2와 같습니다. 화면에 닉네임만 있고 프로필 이미지는 없습니다 [Figma], [정책 추천: U-08].
- 응답: 200, U3 객체. 에러: 중복 409 `CONFLICT`.

### U5. 회원 탈퇴

| 요청 필드 | 타입 | 필수 | 설명 | 출처 |
|---|---|---|---|---|
| agreed | boolean | O | "안내사항을 확인했으며 회원 탈퇴에 동의해요". `true`가 아니면 400 | [Figma] |
| deleteMyRecipes | boolean | X (기본 false) | "내 레시피도 함께 삭제할까요?" | [Figma], 처리는 [정책 추천: U-05] |
| reason | string | X | 탈퇴 사유 | [정책 추천: U-06] 화면정의서 근거. 와이어프레임 v2에는 없음 |

- DELETE에 본문을 싣지 않으려면 `POST /api/v1/users/me/withdrawal`로 바꿀 수 있습니다 [추정].
- 응답: 204 또는 200 `{ success: true }`. 즉시 탈퇴입니다 [정책 추천: U-04].

### T1. 약관 목록 / T2. 약관 상세

| 응답 필드 | 타입 | 출처 |
|---|---|---|
| id | number | [도메인] TermsResponse |
| type | string | [도메인] 현재는 자유 문자열. enum(SERVICE, PRIVACY, AGE_14)은 [추정] |
| version | string | [도메인] |
| contentUrl | string | [도메인] |
| effectiveAt | string(datetime) | [도메인] 화면의 "시행일" |
| createdAt | string(datetime) | [도메인] |
| title | string | [추정] 화면 제목("CUSFIT 서비스 이용약관")에 필요 |
| required | boolean | [추정] "(필수)" 표시에 필요 |

- T1은 종류별 현재 시행 중인 최신 버전만 돌려줍니다 [추정].
- 본문을 `contentUrl`로 두는 것은 현재 코드 구조입니다. 약관 상세 화면은 앱 안 본문(제1조~제4조)을 보여 주므로, 웹뷰로 열지 본문을 내려줄지 정해야 합니다.

## 카탈로그 (브랜드, 메뉴, 옵션 프리셋)

브랜드·메뉴·옵션 프리셋은 테이블과 생성 DTO가 이미 있습니다 [도메인]. 목록·검색 조회와 옵션 그룹(Top/Middle/Bottom)은 아직 없습니다.

| | 메서드 | 경로 | 인증 | 화면 |
|---|---|---|---|---|
| C1 | GET | `/api/v1/brands` | 없음 | 홈(브랜드 선택), 레시피 만들기, 탐색 브랜드 필터 |
| C2 | GET | `/api/v1/menus` | 없음 | 메뉴 선택 |
| C3 | GET | `/api/v1/brands/{brandId}/option-presets` | 없음 | 레시피 만들기, 커스텀 옵션 추가 |
| C4 | POST | `/api/v1/admin/brands` | 관리자 | 없음 (운영용) |
| C5 | POST | `/api/v1/admin/menus` | 관리자 | 없음 (운영용) |
| C6 | POST | `/api/v1/admin/option-presets` | 관리자 | 없음 (운영용) |

### C1. 브랜드 목록

| 응답 필드 (배열 원소) | 타입 | 출처 |
|---|---|---|
| id | number | [도메인] BrandResponse |
| name | string | [도메인] |
| createdAt | string(datetime) | [도메인] |
| logoUrl | string \| null | [추정] 카드에 브랜드 표시가 있어 필요할 수 있음 |

- 브랜드 수가 적어 페이지네이션 없이 전체를 돌려줍니다 [추정]. MVP 브랜드는 스타벅스, 공차 [정책 추천: CA-01], [Figma] 홈 문구.

### C2. 메뉴 검색

`GET /api/v1/menus?brandId=1&keyword=자바칩`

| 파라미터 | 필수 | 설명 | 출처 |
|---|---|---|---|
| brandId | X | 브랜드 필터 | [Figma] "브랜드 필터" |
| keyword | X | 브랜드명 또는 메뉴명 부분 일치 | [Figma] "브랜드 또는 메뉴 검색" |

| 응답 필드 (배열 원소) | 타입 | 출처 |
|---|---|---|
| id, brandId, name, createdAt | | [도메인] MenuResponse |
| brandName | string | [Figma] 메뉴 행에 "브랜드·카테고리" 표시 |
| category | string | [Figma] 같은 문구. 스키마에 없음 |
| temperature | HOT \| ICE \| null | [추정] Hot/Ice를 별도 메뉴로 나누자는 안 [정책 추천: CA-05] 기준이면 이름에 포함되어 필드가 필요 없음 |

- 판매 종료(`active = false`) 메뉴는 검색에서 빠집니다 [정책 추천: CA-03].
- 메뉴 수가 많아질 수 있으므로 offset 페이지네이션을 넣을지 정해야 합니다 [추정].

### C3. 옵션 프리셋 목록

`GET /api/v1/brands/{brandId}/option-presets?group=MIDDLE`

| 응답 필드 (배열 원소) | 타입 | 출처 |
|---|---|---|
| id, brandId, name, createdAt | | [도메인] OptionPresetResponse |
| group | TOP \| MIDDLE \| BOTTOM | [Figma] 레시피 만들기의 Top/Middle/Bottom, [정책 추천: CA-04]. 스키마에 없음 |
| quantifiable | boolean | [추정] "헤이즐넛 시럽 2"처럼 수량이 붙는 옵션인지 |

### C4~C6. 관리자 등록

요청 본문은 기존 개발 API DTO를 그대로 씁니다 [도메인].

- C4 `CreateBrandRequest`: name(string, 필수, 공백 불가)
- C5 `CreateMenuRequest`: brandId(number, 필수), name(string, 필수)
- C6 `CreateOptionPresetRequest`: brandId(number, 필수), name(string, 필수). group을 추가해야 합니다 [추정].

관리자 API 자체는 **[정책 추천: C-03]**이고, 수정·판매 종료 API는 이 초안에서 내렸습니다.

## 레시피 생성 흐름

레시피 생성은 "AI 분석 → 사용자 수정 → 중복 확인 → 저장 → AI 피드백" 4개 API로 나눴습니다. AI 분석 결과는 저장하지 않고, 사용자가 "레시피 저장하기"를 누를 때만 저장합니다 [정책 추천: CON-05].

1. 레시피를 만들 방법을 고릅니다: 사진 촬영, 불러오기, 직접 만들기, 또는 브랜드 + 자연어 입력 [Figma] 홈·카메라 촬영 화면.
2. 사진·불러오기·자연어는 R1(AI 분석)을 호출하고, "AI 분석 중" 화면을 띄웁니다. 직접 만들기는 이 단계를 건너뜁니다.
3. "레시피를 이렇게 정했어요" 화면에서 브랜드, 기본 메뉴(C2), 옵션(C3 + 직접 입력), 레시피 이름을 고칩니다. 브랜드를 바꾸면 메뉴와 옵션이 초기화됩니다 [Figma] 브랜드 변경 경고. 서버 호출 없음.
4. 저장 전에 R2(중복 확인)를 호출합니다. 중복이면 "이미 등록된 레시피에요" 바텀시트를 띄웁니다 [Figma].
5. R3(레시피 저장)을 호출합니다.
6. AI로 만든 레시피면 "AI가 레시피를 잘 정리했나요?" 바텀시트를 띄우고 R4(AI 피드백)를 호출합니다. "나중에 할게요"는 호출하지 않습니다 [정책 추천: R-11].

| | 메서드 | 경로 | 인증 |
|---|---|---|---|
| R1 | POST | `/api/v1/recipes/ai-analysis` | 필수 |
| R2 | POST | `/api/v1/recipes/duplicate-check` | 필수 |
| R3 | POST | `/api/v1/recipes` | 필수 |
| R4 | POST | `/api/v1/recipes/{recipeId}/ai-feedback` | 필수 |

레시피 만들기를 누르면 비로그인 사용자에게 "로그인이 필요해요" 바텀시트가 뜨는 것으로 보고, 네 API 모두 로그인 필수로 두었습니다 [추정].

### 공통 객체: RecipeOption

| 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| name | string | O | 공백 불가 | [도메인] CreateRecipeOptionRequest |
| | | | 직접 입력 최대 200자 | [Figma] "0/200". 30자로 줄이자는 제안 [정책 추천: CA-04] |
| position | number | 응답에만 | 그룹 안 순서. 요청 배열 순서로 서버가 매김 | [도메인] recipe_options.position |
| group | TOP \| MIDDLE \| BOTTOM | O | 그룹별 최대 3개 | [Figma] "0/3", [정책 추천: CA-04]. 스키마에 없음 |
| presetId | number \| null | X | 프리셋에서 고른 경우. 직접 입력이면 null | [정책 추천: CA-04]. 스키마에 없음 |
| quantity | number \| null | X | "헤이즐넛 시럽 2"의 2 | [Figma] 예시, [정책 추천: CA-04]. 스키마에 없음 |

### R1. AI 분석

`multipart/form-data` [추정]

| 요청 필드 | 타입 | 필수 | 설명 | 출처 |
|---|---|---|---|---|
| image | file | 조건부 | 촬영·불러온 이미지. 저장하지 않음 | [Figma], 보관 안 함은 [정책 추천: R-10] |
| text | string | 조건부 | "자바칩 프라푸치노에 휘핑 많이, 자바칩 추가" | [Figma] 카메라 촬영 화면의 자연어 입력 |
| brandId | number | X | "어느 브랜드의 메뉴인가요?" | [Figma] |

- `image`와 `text` 중 하나는 있어야 합니다 [추정]. 이미지 크기·형식 제한은 정해지지 않았습니다.
- 응답 `data`: `{ "analysisId": "...", "brandId": 1, "menuId": 12, "title": "버터비어 프라푸치노", "options": [RecipeOption...] }`. 인식하지 못한 항목은 `null`로 두고 사용자가 직접 입력합니다 [정책 추천: CA-02 근거의 User Flow]. 필드 구성은 [추정].
- `analysisId`는 R4에서 AI 원본 결과와 최종 레시피를 비교하려고 둡니다 [추정]. 원본 결과를 Redis에 잠시 두는 방식을 가정했습니다.
- 에러: 하루 20회 초과 429 `TOO_MANY_REQUESTS` [정책 추천: R-03], 분석 실패 422 `AI_ANALYSIS_FAILED` [추정].

### R2. 중복 확인

- 요청: `{ "menuId": 12, "options": [RecipeOption...] }`
- 응답 `data`: `{ "duplicated": true, "existingRecipe": RecipeSummary | null }` [정책 추천: API-03]. RecipeSummary는 다음 절 R5 참고.
- 판정 기준: 기본 메뉴가 같고 그룹별 옵션 집합이 같으면 중복. 순서·앞뒤 공백·대소문자는 무시 [정책 추천: R-04].
- 범위: 본인 레시피만 [정책 추천: R-04 A안]. 본인 레시피만 막기로 확정했습니다(D3). Figma 경고의 "온도님의 레시피" 문구는 본인 기준에 맞게 고쳐야 합니다.

### R3. 레시피 저장

| 요청 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| menuId | number | O | 판매 중인 메뉴 | [도메인] `@NotNull` |
| title | string | O | 공백 불가 | [도메인] `@NotBlank` |
| | | | 최대 200자 | [Figma] "0/200". 30자 제안 [정책 추천: R-02] |
| options | RecipeOption[] | O | 배열은 null 불가 | [도메인] `@NotNull` |
| | | | 1개 이상 | [정책 추천: R-02]. 현재 코드는 빈 배열을 허용 |
| analysisId | string \| null | X | AI로 만든 경우. 있으면 source = AI | [추정] |

- `brandId`는 보내지 않습니다. 현재 스키마에서 레시피는 `menu_id`만 갖고 브랜드는 메뉴를 통해 정해집니다 [도메인].
- 개발 API의 `authorId` 필드는 운영에서 빼고 토큰의 사용자로 채웁니다 [도메인: TECH_DECISIONS "개발 API는 명시적 사용자 ID를 받습니다"].
- 응답: 201, `data` = R6 상세 객체.
- 에러: 메뉴 없음 404 `NOT_FOUND`, 그룹별 3개 초과 400 `INVALID_REQUEST`, 중복(R2를 건너뛴 경우 최후 방어) 409 `CONFLICT` [정책 추천: API-03], 하루 30개 초과 429 `TOO_MANY_REQUESTS` [정책 추천: R-03].

### R4. AI 피드백

| 요청 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| rating | LIKE \| DISLIKE | O | | [정책 추천: R-11] 좋아요/싫어요 |
| reasons | string[] | DISLIKE일 때 | 여러 개 선택 | 선택지 6개는 [Figma], 복수 선택은 [정책 추천: R-11] |
| etcText | string | reasons에 ETC가 있을 때 | 최대 200자 | [Figma] "10/200" |

- `reasons` 값: `WRONG_MENU`(기본 메뉴가 달라요), `WRONG_OPTION`(옵션이 빠졌거나 잘못됐어요), `WRONG_QUANTITY`(옵션 수량이 달라요), `WRONG_GROUP`(Top/Middle/Bottom 구분이 이상해요), `AWKWARD_TITLE`(레시피 이름이 어색해요), `ETC`(기타). 한글 문구는 [Figma], 영문 코드는 [추정].
- 싫어요 화면의 "건너뛰기"는 사유 없이 DISLIKE만 보낼지, 아무것도 보내지 않을지 정해야 합니다 [추정].
- 레시피당 1회. 두 번째는 409 `CONFLICT`, 직접 만든 레시피는 400 `INVALID_REQUEST` [정책 추천: R-11]. 남의 레시피면 403 `FORBIDDEN` [추정].
- 응답: 201, `data` 없음. 성공 후 "의견 보내주셔서 감사해요" 화면 [Figma].
- AI 피드백 테이블은 스키마에 없습니다.

## 레시피 조회·탐색·좋아요·저장·삭제

현재 `recipes`는 `author_id`, `menu_id`, `title`, `created_at`만 갖습니다 [도메인]. 좋아요·저장 테이블, 카운트 컬럼, 상태(삭제·블라인드) 컬럼은 없습니다.

| | 메서드 | 경로 | 인증 | 화면 |
|---|---|---|---|---|
| R5 | GET | `/api/v1/recipes` | 선택 | 홈 인기 레시피, 탐색, 검색 |
| R6 | GET | `/api/v1/recipes/{recipeId}` | 선택 | 레시피 자세히 보기 (본인/타인) |
| R7 | DELETE | `/api/v1/recipes/{recipeId}` | 필수 | 본인 상세 > 삭제하기 |
| R8 | PUT / DELETE | `/api/v1/recipes/{recipeId}/like` | 필수 | 타인 상세 > 좋아요 |
| R9 | PUT / DELETE | `/api/v1/recipes/{recipeId}/save` | 필수 | 타인 상세 > 저장 |
| R10 | GET | `/api/v1/users/me/recipes` | 필수 | 내 레시피 |
| R11 | GET | `/api/v1/users/me/saved-recipes` | 필수 | 저장한 레시피 |

레시피 수정 API는 넣지 않았습니다. 본인 상세의 더보기에는 "레시피 카드 저장하기"와 "삭제하기"만 있고 수정 진입점이 없습니다 [Figma]. "레시피 카드 저장하기"는 사진 앱에 이미지로 저장하는 기능이라 서버 API가 필요 없다고 봤습니다 [추정].

### RecipeSummary (목록 카드)

| 필드 | 타입 | 출처 |
|---|---|---|
| id, title, createdAt | | [도메인] RecipeResponse |
| menu | { id, name } | menuId는 [도메인], 이름 포함은 [Figma] 카드의 "자바칩 프라푸치노" |
| brand | { id, name } | [Figma] 카드의 "스타벅스" |
| author | { id, nickname } | authorId는 [도메인], 닉네임은 [Figma] "온도". 탈퇴한 사용자는 "탈퇴한 사용자" [정책 추천: C-02] |
| optionPreview | string[] | [Figma] 카드의 "헤이즐넛 시럽 1펌프", "1샷 추가". 몇 개까지 보여 줄지는 [추정] 2개 |
| saveCount | number | [Figma] 카드의 숫자 "98". 저장 수라는 해석은 [정책 추천: R-08], 디자인 확인 필요 |

다른 도메인의 브랜드명·메뉴명·닉네임은 ID로 모아 한 번씩 조회해 합칩니다 [정책 추천: API-04].

### R5. 레시피 목록·검색

`GET /api/v1/recipes?keyword=자바칩&brandId=1&sort=POPULAR&page=0&size=20`

| 파라미터 | 필수 | 설명 | 출처 |
|---|---|---|---|
| keyword | X | 브랜드·메뉴·레시피·작성자 부분 일치 | [Figma] 검색창 문구, 구현은 pg_trgm [정책 추천: API-02] |
| brandId | X | 브랜드 필터 | [Figma] |
| sort | X | POPULAR(기본) \| LATEST | [정책 추천: R-08] "인기순(기본)·최신순" |
| page / cursor, size | X | 인기순은 page, 최신순은 cursor | [정책 추천: API-01] |

- 응답: `{ "items": RecipeSummary[], "hasNext": true, "nextCursor": ... }`. 결과가 없으면 빈 배열이고, 앱이 "검색 결과가 없어요"를 보여 줍니다 [Figma].
- 홈 "인기 레시피"는 같은 API를 `sort=POPULAR&size=5`로 부릅니다 [추정].
- 블라인드·삭제된 레시피는 빠집니다 [정책 추천: RP-04, C-01].
- 탐색 화면의 "개수"는 레이어 이름만 확인했고 무엇을 세는지는 모릅니다. 검색 결과 수라면 `totalCount`가 필요합니다.

### R6. 레시피 상세

RecipeSummary에 아래 필드를 더합니다.

| 필드 | 타입 | 출처 |
|---|---|---|
| options | { top: RecipeOption[], middle: [...], bottom: [...] } | 옵션 목록은 [도메인], 그룹 나눔은 [Figma] 상세 화면의 Top/Middle/Bottom |
| isMine | boolean | [Figma] "내가 등록한 레시피" / "온도님의 레시피" 분기. 필드명은 [추정] |
| likeCount | number | [Figma] 좋아요 버튼. 숫자 표시 여부는 모름 |
| liked, saved | boolean | [추정] 버튼 상태. 비로그인이면 false |

- 에러: 없거나 삭제됨 404 `NOT_FOUND`, 블라인드 `RECIPE_BLINDED` [추정]. 작성자 본인은 블라인드된 레시피도 볼 수 있습니다 [정책 추천: RP-04].
- 공유 URL용 슬러그(`/r/{slug}`)는 정책 문서 R-09에 있지만 와이어프레임 v2에 공유 버튼이 없어 넣지 않았습니다.

### R7. 레시피 삭제

- 응답: 204. "해당 작업은 돌릴 수 없습니다" 확인 모달 후 호출 [Figma].
- 에러: 작성자가 아님 403 `FORBIDDEN`, 없음 404 `NOT_FOUND`.
- 레시피는 soft delete 후 30일 뒤 물리 삭제, 좋아요·저장은 즉시 삭제 [정책 추천: C-01, R-07].

### R8. 좋아요 / R9. 저장

- PUT은 누르기, DELETE는 취소. 이미 눌렀는데 다시 PUT이 와도 성공을 돌려주고 카운트는 올리지 않습니다 [정책 추천: CON-03].
- 응답 `data`: `{ "liked": true, "likeCount": 13 }` / `{ "saved": true, "saveCount": 99 }` [추정].
- 본인 레시피면 403 `FORBIDDEN` [정책 추천: R-06], 화면도 본인 상세에 두 버튼이 없습니다 [Figma].

### R10. 내 레시피 / R11. 저장한 레시피

- 파라미터: `brandId`(브랜드 필터 [Figma]), `cursor`, `size`. 최신순 고정 [추정].
- 응답: `{ "items": RecipeSummary[], "hasNext": true, "nextCursor": ..., "totalCount": 12 }`. 두 화면에 "개수"가 표시되어 `totalCount`를 넣었습니다 [Figma], [정책 추천: API-01].
- R10은 블라인드된 내 레시피도 `status: BLINDED`로 포함 [정책 추천: RP-04]. R11은 삭제·블라인드된 레시피를 뺍니다 [정책 추천: R-07].
- 빈 목록이면 "아직 만든 레시피가 없어요", "저장한 레시피가 없어요" 화면 [Figma].

## 신고·알림·고객지원

세 도메인 모두 테이블과 생성 DTO는 있지만, 화면이 요구하는 필드(신고 사유, 알림 종류, 문의 유형 등)는 아직 없습니다 [도메인].

| | 메서드 | 경로 | 인증 | 화면 |
|---|---|---|---|---|
| P1 | POST | `/api/v1/recipes/{recipeId}/reports` | 필수 | 신고하기 |
| N1 | GET | `/api/v1/notifications` | 필수 | 알림 |
| N2 | GET / PUT | `/api/v1/users/me/notification-settings` | 필수 | 알림 설정 |
| N3 | PUT / DELETE | `/api/v1/users/me/device-tokens` | 필수 | 알림 권한 허용, 로그아웃 (화면 없음) |
| S1 | GET | `/api/v1/notices` | 없음 | 공지사항 |
| S2 | GET | `/api/v1/notices/{noticeId}` | 없음 | 공지사항 상세 |
| S3 | GET | `/api/v1/faqs` | 없음 | 고객센터 |
| S4 | POST | `/api/v1/inquiries` | 필수 | 1:1 문의하기 |
| S5 | POST | `/api/v1/admin/notices`, `/api/v1/admin/faqs` | 관리자 | 없음 (운영용) |

### P1. 레시피 신고

| 요청 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| reason | INAPPROPRIATE \| AD \| HATE \| PLAGIARISM \| ETC | O | 하나 선택 | 사유 5개는 [정책 추천: RP-02](화면정의서 근거), 영문 코드는 [추정]. 와이어프레임 v2는 "사유" 자리표시만 있음 |
| content | string | 아래 참고 | 최대 200자 | [정책 추천: RP-02] |

- `content`는 현재 코드에서 필수(`@NotBlank`)인데 [도메인], 정책 문서는 "선택 입력"입니다. 둘 중 하나로 맞춰야 합니다.
- `reporterId`는 토큰에서, `recipeId`는 경로에서 받습니다. 두 필드는 [도메인] CreateReportRequest.
- 에러: 본인 레시피 403 `FORBIDDEN` [정책 추천: RP-01], 같은 레시피 재신고 409 `CONFLICT` [정책 추천: RP-03].
- 응답: 201, `data` 없음. 신고자는 작성자에게 공개하지 않습니다 [정책 추천: RP-02].

### N1. 알림 목록

`GET /api/v1/notifications?tab=ACTIVITY&cursor=...&size=20`

| 필드 | 타입 | 출처 |
|---|---|---|
| id, title, content, createdAt | | [도메인] NotificationResponse. 예: "회원님의 레시피가 100번 저장됐어요" / "두유라떼 (+샷 추가)" [Figma] |
| category | ACTIVITY \| NEWS | [Figma] 탭 "전체·활동·소식". 스키마에 없음 |
| targetRecipeId | number \| null | [추정] 눌렀을 때 이동할 레시피 |

- `tab` 파라미터 없으면 전체. `recipientId`는 토큰의 사용자라 응답에서 뺍니다 [추정].
- 읽음 표시(`isRead`)는 화면 텍스트에서 확인되지 않아 넣지 않았습니다. 디자인에 미확인 점 표시가 있다면 추가해야 합니다.

### N2. 알림 설정

- 필드: `all`, `activity`(레시피 활동), `news`(브랜드·레시피 소식), `service`(서비스 알림), 모두 boolean. 항목은 [Figma], 필드명은 [추정].
- PUT은 바꾼 항목만 보낼지(PATCH) 전부 보낼지 정해야 합니다. `all`은 나머지 세 개를 한 번에 바꾸는 스위치로 보고 서버에 따로 저장하지 않을 수 있습니다 [추정].
- "저장한 브랜드의 인기 레시피" 문구는 브랜드 저장 기능이 없어 의미가 정해지지 않았습니다 [정책 추천: N-03].

### N3. 기기 토큰

- PUT 요청: `{ "token": "<FCM 토큰>", "platform": "IOS" | "ANDROID" }`. 로그아웃 때 DELETE로 그 기기 토큰만 지웁니다 [정책 추천: N-05]. 경로와 필드명은 [추정].

### S1~S3. 공지·FAQ

| API | 응답 필드 | 출처 |
|---|---|---|
| S1, S2 공지 | id, title, content, createdAt | [도메인] NoticeResponse |
| S1, S2 공지 | important(boolean) | [Figma] "중요" 배지, 목록 상단 고정은 [정책 추천: S-01]. 스키마에 없음 |
| S3 FAQ | id, question, answer, createdAt | [도메인] FaqResponse |

- S1은 목록에서 `content`를 빼도 됩니다 [추정]. 정렬은 중요 공지 먼저, 그다음 등록일 최신순 [정책 추천: S-01].
- S3는 `keyword`로 질문·답변 부분 일치 검색을 받습니다 [정책 추천: S-03].

### S4. 1:1 문의

`multipart/form-data` [추정]

| 요청 필드 | 타입 | 필수 | 규칙 | 출처 |
|---|---|---|---|---|
| type | string(enum) | O | 문의 유형 | [Figma] "문의 유형을 선택해주세요". 유형 목록은 [정책 추천: S-02]. 스키마에 없음 |
| content | string | O | 최대 500자 | 필수는 [도메인] `@NotBlank`, 500자는 [Figma] |
| images | file[] | X | 최대 3장, 장당 10MB | 첨부는 [Figma], 제한은 [정책 추천: S-02] |

- 현재 DTO에는 `title`이 필수인데 [도메인], 1:1 문의 화면에는 제목 입력란이 없습니다 [Figma]. 제목을 빼거나 유형으로 대신해야 합니다.
- 답변 이메일은 입력받지 않고 가입 이메일을 씁니다 [Figma] "가입한 이메일로 답변을 보내드려요". 앱 안 문의 내역 조회 API는 두지 않습니다 [정책 추천: S-02].

### S5. 관리자 등록

요청 본문은 `CreateNoticeRequest(title, content)`, `CreateFaqRequest(question, answer)`를 그대로 씁니다 [도메인]. 앱 정보 화면의 버전·저작권은 앱에 들어가는 값으로 보고 API를 두지 않았습니다 [추정].

## 결정 필요 목록

개발 전에 정해야 할 것들입니다. 위쪽 4개는 스키마와 인증 구조에 걸려 있어 가장 먼저 정해야 합니다.

| | 결정할 것 | 이 초안의 가정 | 관련 API |
|---|---|---|---|
| D1 | 토큰 발급자: Firebase Auth vs 서버 자체 JWT | Firebase Auth (A-01). 서버 로그인 API 없음 | 전체 |
| D2 | 비로그인 허용 범위 | 목록·상세·카탈로그·공지·FAQ·약관만 허용 | R5, R6, C1~C3, S1~S3 |
| D3 | 중복 레시피 범위: 본인만 vs 전체 | 확정: 본인만 (R-04 A). Figma 경고 문구 수정 필요 | R2, R3 |
| D4 | 옵션 저장 구조: 그룹·프리셋 ID·수량 추가 | 추가함 (CA-04) | C3, R1~R3, R6 |
| D5 | 레시피 이름·직접 입력 옵션 길이: 200자 vs 30자 | Figma대로 200자 | R3 |
| D6 | 카드 숫자 "98"이 저장 수인지, 인기순 기준 | 저장 수 (R-08) | R5, R6 |
| D7 | 레시피 수정 기능을 둘지 | 두지 않음 (Figma에 진입점 없음) | 없음 |
| D8 | AI 피드백 "건너뛰기" 처리 | 미정 | R4 |
| D9 | 신고 상세 내용 필수 여부 | 코드는 필수, 정책은 선택 | P1 |
| D10 | 1:1 문의 제목 필드 | 코드는 필수, 화면에는 없음 | S4 |
| D11 | 약관 본문: URL(웹뷰) vs 본문 텍스트 | 현재 코드대로 URL | T2 |
| D12 | 만 14세 확인을 약관으로 다룰지 별도 필드로 둘지 | 약관 항목 | U2, T1 |
| D13 | AI 분석 실패 화면과 에러 코드 | AI_ANALYSIS_FAILED, 화면 없음 | R1 |
| D14 | 운영 API 접두사와 새 에러 코드 추가 | `/api/v1`, TOO_MANY_REQUESTS 등 추가 | 전체 |

## 현재 스키마에 없는 항목

이 초안대로 구현하려면 아래를 Flyway V8 이후 마이그레이션으로 추가해야 합니다. 이미 적용된 V1~V7은 수정하지 않는 규칙입니다 [도메인: CLAUDE.md].

| 테이블 | 추가할 것 | 필요한 API |
|---|---|---|
| users | 이메일, 소셜/인증 UID, 상태(탈퇴) | U2, U3, U5 |
| 신규 | 약관 동의 이력 | U2 |
| terms | 제목, 필수 여부 | T1 |
| menus | 카테고리, 판매 여부 | C2 |
| option_presets | 그룹 | C3 |
| recipes | 상태(블라인드·삭제), 좋아요·저장 수, 생성 방식(AI/직접), 중복 판정용 시그니처 | R3, R5, R6 |
| recipe_options | 그룹, 프리셋 ID, 수량 | R3, R6 |
| 신규 | 좋아요, 저장, AI 피드백 | R4, R8, R9, R11 |
| reports | 사유, 처리 상태 | P1 |
| notifications | 카테고리, 대상 레시피 | N1 |
| 신규 | 알림 설정, 기기 토큰 | N2, N3 |
| notices | 중요 여부 | S1 |
| inquiries | 유형, 첨부 이미지 | S4 |
