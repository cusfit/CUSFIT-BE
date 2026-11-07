# 작업: scenario-report-blind — SC-04 신고 누적과 자동 블라인드

## 배정
- 작업 ID: scenario-report-blind
- 선행 작업: scenario-infra, report 신고 접수·상태·자동 블라인드, recipe 상태 `BLINDED`와 목록 조회, notification 블라인드 통지
- 테스트 클래스: `scenario/ReportBlindScenarioTest`
- 검증 대상: `'*ReportBlindScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- `ReportWriteService.report(Long reporterId, Long recipeId, ReportReason reason, String detail)` — 사유 enum 5개 (RP-02)
- 자동 블라인드 정책: 서로 다른 신고자 수가 임계값에 도달하면 `BLINDED` + 운영 검토 대기열 (RP-04). 임계값은 상수·설정값
- `(reporter_id, recipe_id)` 유니크 (RP-03)
- 운영자 복구·삭제 확정 (RP-04, C-03)
- 블라인드 통지 알림. 알림 설정으로 끌 수 없음 (RP-05, N-01)
- 탐색, 검색, 홈, 저장 목록, 내 레시피 목록 조회

## 시나리오
Given: A의 레시피 R, 회원 B·C·D. B가 R을 저장해 둠.

When: B, C, D가 차례로 R을 신고한다. (임계값 `THRESHOLD`를 기준으로 `THRESHOLD - 1`명, `THRESHOLD`명째로 씀)

Then:
- `THRESHOLD - 1`명까지는 R이 `ACTIVE` (RP-04)
- `THRESHOLD`명째 신고로 R이 `BLINDED`가 되고 운영 검토 대기열에 오름 (RP-04)
- 탐색, 검색, 홈, B의 저장 목록에서 R이 빠짐 (RP-04)
- A의 "내 레시피" 목록에는 "블라인드됨"으로 보임 (RP-04)
- 공유 슬러그 조회는 "블라인드됨" 상태를 돌려줌 (R-09)
- A에게 블라인드 통지 알림이 1건 생기고, A가 레시피 활동 알림을 꺼 두었어도 생김 (RP-05, N-01)

## 변형
- `[RP-03]` B가 같은 레시피를 다시 신고하면 `CONFLICT`. 처리 상태와 관계없이
- `[RP-01]` A가 자기 레시피를 신고하면 거부
- `[RP-04]` 같은 사람이 여러 번 신고한 것은 "서로 다른 N명"으로 세지 않음 (RP-03이 막지만, 정책 계산 자체도 L1 `AutoBlindPolicyTest`에서 확인)
- `[RP-04]` 운영자가 복구하면 `ACTIVE`로 돌아가고 목록에 다시 보임
- `[RP-04]` 운영자가 삭제 확정하면 SC-03 연쇄 (`scenario-recipe-deletion`과 중복이면 여기서는 상태만 확인)
- `[RP-02]` 신고자 정보가 작성자용 조회 응답 어디에도 없음
- `[RP-05]` 운영자가 삭제 확정하면 A에게 삭제 통지 알림 1건
- `[U-06]` 신고자가 탈퇴해 `reporter_id`가 NULL이 된 신고도 신고자 수에 그대로 포함되는지 (아래 결정 대기)

## 결정 대기
- RP-04 임계값 3명은 출시 후 조정 대상. 테스트는 상수를 참조합니다.
- 블라인드된 레시피에 대한 추가 신고를 받을지 정책에 없습니다.
- 신고자 탈퇴 후 그 신고를 "서로 다른 신고자"로 계속 셀지 정책에 없습니다.
- RP-01 본인 레시피 신고 거부의 `ErrorCode`.
