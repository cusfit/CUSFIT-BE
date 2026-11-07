# 작업: scenario-reaction-milestone — SC-05 좋아요·저장과 마일스톤 알림

## 배정
- 작업 ID: scenario-reaction-milestone
- 선행 작업: scenario-infra, recipe 좋아요·저장(원자적 카운터), notification 마일스톤 알림·알림 설정·푸시 발송
- 테스트 클래스: `scenario/ReactionMilestoneScenarioTest`
- 검증 대상: `'*ReactionMilestoneScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- `LikeWriteService.like/unlike(Long userId, Long recipeId)`, `BookmarkWriteService.bookmark/unbookmark(...)` — PUT/DELETE 멱등 (CON-03)
- `recipe.like_count`, `recipe.save_count` 원자적 UPDATE (CON-01, R-08)
- 마일스톤 정책: 1, 10, 50, 100, 이후 100 단위. 레시피별 마지막 알림 기준을 저장 (N-02, CON-06). 목록은 상수
- 알림 설정 "레시피 활동" (N-01)
- 푸시 발송 Fake `FakePushSender` (`scenario-infra`)

## 시나리오
Given: A의 레시피 R, 다른 회원 여러 명.

When: 다른 회원들이 R을 저장한다.

Then:
- 저장 수가 마일스톤(1, 10, 50, 100, 200)에 도달하는 순간에만 A에게 활동 알림이 1건씩 생김 (N-02)
- 마일스톤 사이(예: 2, 9, 11)에는 알림이 없음 (N-02)
- 알림 문구에 저장한 사람의 닉네임이 없음 (C-04)
- R의 `save_count`가 실제 저장 행 수와 같음 (CON-01)

200까지 실제로 저장하면 회원 200명이 필요합니다. 마일스톤 판정은 L1 `MilestonePolicyTest`에서 전부 확인하고, 시나리오는 1·10 경계와 "다시 넘어도 재발송 없음"만 실제 경로로 확인하는 편이 빠릅니다. 큰 숫자는 `save_count`를 Fixture로 `마일스톤 - 1`에 맞춘 뒤 한 번 저장해 확인합니다.

## 변형
- `[N-02]` 10번째 저장이 취소되고 다시 저장되어 10을 다시 넘어도 알림이 다시 가지 않음
- `[R-06]` A가 자기 레시피에 좋아요·저장하면 거부
- `[CON-03]` 같은 사람이 저장 PUT을 두 번 보내도 성공, 카운트는 1
- `[CON-03]` 저장하지 않은 상태에서 DELETE를 보내도 성공, 카운트는 0 — 정책은 PUT의 멱등만 명시. 이 줄은 가이드의 제안
- `[N-01]` A가 "레시피 활동" 알림을 끄면 `FakePushSender` 기록 0건
- `[U-06]` 탈퇴한 사용자가 남긴 레시피가 마일스톤을 넘으면 푸시가 가지 않음 (토큰 삭제됨)
- `[N-04]` 내 행동으로 생긴 알림은 없음 (R-06으로 막히므로 위 거부 테스트와 함께 확인)
- 좋아요도 같은 마일스톤 알림을 보내는지는 아래 결정 대기

## 결정 대기
- R-06 본인 레시피 좋아요의 `ErrorCode` (`FORBIDDEN` 또는 `INVALID_REQUEST`)
- 알림을 끈 사용자에게 알림함 기록은 남길지 (푸시만 막을지)
- N-02 마일스톤 목록 확정
- 좋아요에도 마일스톤 알림을 보낼지. N-01 예시는 "저장"이고 표 항목은 "좋아요·저장"입니다.
