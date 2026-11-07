# 작업: scenario-recipe-create — SC-06 레시피 작성과 중복 판정

## 배정
- 작업 ID: scenario-recipe-create
- 선행 작업: scenario-infra, catalog 메뉴 활성 상태·옵션 그룹, recipe 옵션 시그니처·중복 확인 API·일일 생성 제한
- 테스트 클래스: `scenario/RecipeCreateScenarioTest`
- 검증 대상: `'*RecipeCreateScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- 레시피 생성: 브랜드, 기본 메뉴, 이름, 그룹(Top/Middle/Bottom)별 옵션 `(프리셋 ID 또는 NULL, 표시 이름, 수량, 순서)` (R-02, CA-04)
- 옵션 시그니처 정규화(순서·앞뒤 공백·대소문자 무시)와 `(author_id, signature)` 부분 유니크 인덱스, 삭제된 행 제외 (R-04, DEL-02)
- 중복 확인 API: 성공 응답으로 `duplicated`와 기존 레시피 ID (API-03)
- `menu.active` (CA-03)
- 일일 생성 제한 30개, 초과 시 새 `ErrorCode` `TOO_MANY_REQUESTS` (R-03)

현재 `Recipe.create(authorId, menuId, title, optionNames)`는 옵션 이름 목록만 받습니다. 그룹·프리셋·수량은 recipe 기능 작업에서 추가해야 합니다.

## 시나리오
Given: 스타벅스 브랜드, 메뉴 M(활성), 메뉴 M2(`active = false`), 옵션 프리셋 몇 개. A가 M + {헤이즐넛 시럽 2, 샷 추가}로 만든 레시피 R1.

When / Then:
- `[R-04]` A가 같은 옵션을 순서만 바꿔 만들면 중복
- `[R-04]` 옵션 이름의 앞뒤 공백·대소문자만 다르면 중복
- `[R-04]` 직접 입력 옵션이 한 글자라도 다르면 중복 아님
- `[R-04]` B가 A와 같은 조합으로 만들면 허용 (A안)
- `[DEL-02]` A가 R1을 지운 뒤 같은 조합으로 만들면 허용
- `[API-03]` 중복 확인 API는 실패가 아닌 성공 응답으로 `duplicated: true`와 기존 레시피 ID를 돌려줌
- `[R-04]` 저장 API에서 중복이면 `CONFLICT` (최후 방어)
- `[R-02]` 필수 항목(브랜드, 기본 메뉴, 이름, 옵션 1개 이상) 중 하나라도 없으면 `INVALID_REQUEST`
- `[CA-04]` 한 그룹에 옵션 4개면 거부
- `[CA-02]` 카탈로그에 없는 메뉴 ID로 만들면 거부 (기본 메뉴는 자유 입력 불가)
- `[CA-03]` M2를 고르면 거부. M2를 쓰던 기존 레시피는 그대로 보이고 "판매 종료" 표시
- `[CA-04]` 운영자가 프리셋 이름을 바꿔도 R1의 옵션 표시 이름은 그대로
- `[R-03]` 하루 `LIMIT`개까지는 성공, `LIMIT + 1`번째는 `TOO_MANY_REQUESTS`

## 결정 대기
- **R-04 중복 범위(A·B·C)**: 스키마에 들어가므로 recipe 엔티티 작업 전에 정해야 합니다. B·C안이면 "B가 같은 조합으로 만들면 허용" 줄이 바뀝니다. 결정 전에는 이 줄을 `@Disabled("R-04 결정 대기")`로 둡니다.
- R-03 `TOO_MANY_REQUESTS` `ErrorCode` 추가와 HTTP 429 매핑 (컨벤션 2.3)
- R-03 일일 경계를 KST 자정으로 볼지 (D-03). 맞다면 SC-09처럼 `MutableClock`으로 경계를 확인합니다.
- CA-04 직접 입력 옵션 최대 길이(200자 또는 30자), R-02 레시피 이름 최대 길이(200자 또는 30자)
