# 작업: scenario-recipe-deletion — SC-03 레시피 하나가 사라질 때 연쇄 동작

## 배정
- 작업 ID: scenario-recipe-deletion
- 선행 작업: scenario-infra, recipe 삭제·좋아요·저장·공유 슬러그, report 신고 상태, notification 대상 조회, 운영자 삭제 확정(C-03), user 탈퇴(SC-02 경로)
- 테스트 클래스: `scenario/RecipeDeletionScenarioTest`
- 검증 대상: `'*RecipeDeletionScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- 작성자 삭제: `RecipeWriteService.delete(Long userId, Long recipeId)`
- 탈퇴 시 삭제: `UserWriteService.withdraw(..., 레시피 함께 삭제)`
- 운영자 삭제 확정: 신고 처리 서비스의 삭제 확정 메서드 (RP-04, C-03)
- 세 진입점이 같은 삭제 경로를 탄다 (정책 9장)
- `recipe.status`(`ACTIVE`, `BLINDED`, `DELETED`), `recipe.deleted_at` (C-01, DEL-01)
- 신고 상태 `TARGET_DELETED` (RP-04)
- 공유 슬러그 조회가 "삭제됨" 상태를 돌려줌 (R-09)
- `(author_id, signature)` 부분 유니크 인덱스, 삭제된 행 제외 (R-04, DEL-02)

## 시나리오
Given:
- A의 레시피 R과 옵션 3개
- B, C가 R에 좋아요·저장
- R에 대한 AI 피드백, 처리 중인 신고 1건, R을 가리키는 알림 1건

When (`@ParameterizedTest`로 세 진입점):
1. A가 직접 삭제
2. A가 삭제를 고르고 탈퇴
3. 운영자가 삭제 확정

Then (세 경우 모두 같음):
- R은 `status = DELETED`, `deleted_at`이 채워짐. 행과 옵션은 남음 (C-01)
- R의 좋아요·저장 행 0개, B와 C의 저장 목록에서 사라짐 (R-07)
- AI 피드백은 남음 (정책 9장)
- 신고는 `TARGET_DELETED` (정책 9장)
- 알림은 남고, 그 알림의 대상 조회는 "삭제됨" 상태를 돌려줌 (N-04)
- 공유 슬러그로 조회하면 "삭제됨" 상태를 돌려줌 (R-09)
- A가 같은 메뉴·옵션으로 새 레시피를 만들 수 있음 (DEL-02)

세 진입점을 하나의 파라미터 테스트로 돌리는 이유는 "경로가 하나"라는 약속 자체를 검증하기 위해서입니다. 진입점 하나만 결과가 다르면 이 테스트가 깨져야 합니다.

## 변형
- `[R-07]` 이미 삭제된 레시피를 다시 삭제하면 거부 (`ErrorCode`는 기능 작업에서 확정)
- `[R-07]` 다른 사람이 삭제하면 `FORBIDDEN`
- `[C-01]` 삭제된 레시피는 탐색·검색·홈 목록에서 빠짐
- 실패 경로: 좋아요·저장 삭제 단계에서 예외가 나면 레시피 상태도 `ACTIVE`로 그대로 (같은 트랜잭션)

## 결정 대기
- C-01 B안(레시피 soft delete, 부속 데이터 hard delete)을 전제로 합니다. A·C안이면 Then 전체가 바뀝니다.
- R-07 A안(저장 목록에서 조용히 사라짐)을 전제로 합니다.
- C-03 운영자 기능 방식(관리자 API 여부)이 정해져야 진입점 3이 확정됩니다.
