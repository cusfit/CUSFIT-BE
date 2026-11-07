# 작업: scenario-recipe-update — SC-07 레시피 수정

## 배정
- 작업 ID: scenario-recipe-update
- 선행 작업: scenario-recipe-create, recipe 수정, 저장 목록 조회
- 테스트 클래스: `scenario/RecipeUpdateScenarioTest`
- 검증 대상: `'*RecipeUpdateScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- `RecipeWriteService.update(Long userId, Long recipeId, UpdateRecipeCommand command)`
- 수정 후 중복 판정 재실행 (R-05, R-04)
- 저장은 레시피 ID만 가리킴 (R-05 A안)

## 시나리오
- `[R-05]` A가 R을 고치면 R을 저장한 B의 목록에서도 바뀐 내용이 보임
- `[R-05][R-04]` 수정 결과가 A의 다른 레시피와 중복이면 거부
- `[R-05]` 다른 사람이 수정하면 `FORBIDDEN`
- `[U-05]` 탈퇴한 사용자가 남긴 레시피는 누구도 수정할 수 없음
- `[R-05]` 수정해도 `like_count`, `save_count`는 그대로
- 수정 중 좋아요가 들어와도 `like_count`가 줄지 않는지는 `scenario-concurrency` CON-01이 맡음

## 결정 대기
- R-05 A안(저장은 ID 참조)을 전제로 합니다. B안(스냅샷)이면 첫 줄이 반대가 됩니다.
- 블라인드된 레시피를 작성자가 수정할 수 있는지 정책에 없습니다.
