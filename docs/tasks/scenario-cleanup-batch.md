# 작업: scenario-cleanup-batch — SC-10 정리 배치

## 배정
- 작업 ID: scenario-cleanup-batch
- 선행 작업: scenario-infra(`MutableClock`), scenario-recipe-deletion, notification 보관 기간, report `recipe_id` NULL 허용·제목 복사
- 테스트 클래스: `scenario/CleanupBatchScenarioTest`
- 검증 대상: `'*CleanupBatchScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- 삭제 후 30일 지난 레시피 물리 삭제 (C-01, DEL-01)
- 90일 지난 알림 삭제 (N-04)
- `reports.recipe_id` NULL 허용, 신고에 레시피 제목 복사 (D-05)
- `recipe_options.recipe_id ... ON DELETE CASCADE` (D-06, 현재 V4에 이미 있음)
- 스케줄러는 Write 서비스 메서드에 위임하고, 배치는 멱등 (OPS-04, 컨벤션 3.4)

## 진입 방법
`@Scheduled` 타이밍을 기다리지 않고, 스케줄러가 위임하는 Write 서비스 메서드를 직접 호출합니다. 시간은 `MutableClock`으로 옮깁니다.

## 시나리오
- `[C-01]` 삭제 후 `RETENTION - 1`일 된 레시피는 남고, `RETENTION`일 된 레시피는 물리 삭제됨 (경계 양쪽 모두. `RETENTION` = 30일)
- `[C-01]` `ACTIVE`, `BLINDED` 레시피는 오래돼도 지워지지 않음
- `[D-05]` 신고가 걸린 레시피도 물리 삭제되고, 신고 행은 `recipe_id`가 NULL이 된 채 복사해 둔 제목을 유지
- `[D-06]` 옵션 행도 함께 사라짐
- `[N-04]` 90일 지난 알림 삭제, 89일은 유지
- `[OPS-04]` 배치를 두 번 연속 실행해도 결과가 같음
- 컨벤션 3.4: 스케줄러 메서드가 예외를 던져도 다음 실행이 막히지 않음
- `[DEL-04]` DB에 없는 S3 파일 정리 배치가 생기면 같은 클래스에 추가

## 결정 대기
- C-01 30일, N-04 90일은 추천안입니다.
- 물리 삭제 대상 레시피의 AI 피드백 처리. 정책 9장은 "보관"이지만 물리 삭제 후 AI 피드백의 `recipe_id`를 어떻게 할지는 정하지 않았습니다(D-05처럼 NULL 허용이 필요할 수 있음).
- 30일 기준 시각을 `deleted_at`으로 볼지 (추천안 문맥상 `deleted_at`)
