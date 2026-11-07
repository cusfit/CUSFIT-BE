# 작업: scenario-concurrency — 동시성 테스트

## 배정
- 작업 ID: scenario-concurrency
- 선행 작업: scenario-infra(`ConcurrencyRunner`), 각 항목의 기능 작업
- 테스트 클래스: 대상별로 나눔 (`scenario/RecipeCounterConcurrencyTest`, `scenario/NicknameConcurrencyTest`, `scenario/MilestoneConcurrencyTest`, `scenario/ReportBlindConcurrencyTest`, `scenario/WithdrawalConcurrencyTest`)
- 검증 대상: `'*ConcurrencyTest'`

가이드 12장의 순서대로, CON-01과 CON-02는 recipe 엔티티 직후에 씁니다. TECH_DECISIONS가 CON-01을 "recipe 전에 반드시"로 표시했습니다.

## 시나리오

| 정책 | 상황 | 기대 결과 |
|---|---|---|
| CON-01 | 작성자가 레시피를 읽어 둔 뒤 수정하는 동안 다른 사람 10명이 좋아요 | `like_count = 10`. 수정 커밋이 카운트를 덮어쓰지 않음 |
| CON-02, U-02 | 같은 닉네임(대소문자만 다름)으로 동시에 두 명 가입 | 한 명 성공, 한 명 `CONFLICT`. `DataIntegrityViolationException`이 500으로 새지 않음 |
| CON-02, CON-03 | 같은 사용자가 같은 레시피에 좋아요 PUT을 동시에 두 번 | 좋아요 행 1개, 카운트 1, 두 요청 모두 성공 |
| CON-06 | 저장 수 99인 레시피에 동시에 5명이 저장 | "100번 저장" 알림이 정확히 1건 |
| RP-04 | 신고 `THRESHOLD - 1`건인 레시피에 서로 다른 두 명이 동시에 신고 | 블라인드 전환 1회, 작성자 통지 1건 |
| DEL-03 | 탈퇴 처리 중 다른 사람이 같은 레시피에 좋아요 | 카운트가 실제 좋아요 행 수와 같고 음수가 아님 |

## 작성 규칙
- `@Transactional`을 붙이지 않습니다. 각 스레드가 자기 트랜잭션을 커밋해야 경쟁이 생깁니다.
- 모든 스레드를 `CountDownLatch`로 같은 순간에 출발시킵니다(`ConcurrencyRunner`).
- CON-01처럼 "읽고 나서 쓰기 전" 틈을 노리는 경우는 우연에 맡기지 않고 순서를 강제합니다. 수정 트랜잭션을 엔티티를 읽은 직후 멈추게 하고, 그 사이 좋아요를 커밋한 뒤 수정을 커밋합니다.
- 결과는 카운트 컬럼만 보지 말고 실제 행 수와 함께 비교합니다: `like_count == count(recipe_likes)`.
- `@RepeatedTest(5)` 정도로 반복합니다. CI 시간이 늘어나면 `@Tag("concurrency")`로 분리합니다.
- 스레드 수는 커넥션 풀 크기(기본 10, CON-05) 안에서 잡습니다. 넘기면 경쟁 대신 커넥션 대기 시간을 재게 됩니다.

## 결정 대기
- CON-01을 "읽은 직후 멈추기"로 강제하는 방법(테스트 전용 훅, 별도 트랜잭션 템플릿 등)은 recipe 수정 구현이 생긴 뒤 정합니다. 운영 코드에 테스트용 훅을 넣지 않는 방법을 우선합니다.
- RP-04 동시 신고에서 블라인드 전환을 1회로 보장하는 방식(조건부 UPDATE, 락)은 report 기능 작업에서 정합니다.
