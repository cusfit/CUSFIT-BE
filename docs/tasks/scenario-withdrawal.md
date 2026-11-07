# 작업: scenario-withdrawal — SC-01, SC-02 회원 탈퇴

## 배정
- 작업 ID: scenario-withdrawal
- 선행 작업: scenario-infra, 그리고 탈퇴에 참여하는 모든 도메인의 기능 작업 (user 탈퇴, recipe 좋아요·저장·삭제, terms 동의 이력, report 신고 처리, notification 설정·토큰, support 문의 첨부)
- 테스트 클래스: `scenario/WithdrawalScenarioTest`
- 검증 대상: `'*WithdrawalScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- `UserWriteService.withdraw(Long userId, WithdrawCommand command)` — `command`는 레시피 함께 삭제 여부와 탈퇴 사유(선택)
- `UserWithdrawnEvent` 발행, 각 도메인의 리스너(같은 트랜잭션에서 DB 정리, `AFTER_COMMIT`에서 S3 삭제·Apple 토큰 철회) (U-06)
- `users.status`(`ACTIVE`, `WITHDRAWN`), 개인정보 컬럼 NULL 허용 (C-02)
- `social_accounts`, 좋아요·저장, 약관 동의 이력, 알림 설정, FCM 토큰, 문의 첨부, AI 피드백, 탈퇴 사유 저장소
- 레시피 상세 조회의 작성자 표시("탈퇴한 사용자")와 `likeCount`, `saveCount`
- 외부 연동 Fake: `FakeFileStorage`, `FakeAppleTokenRevoker` (`scenario-infra`)

## 공통 Given
- 회원 A(Apple 로그인), B, C
- A의 레시피 R1, B의 레시피 R2
- A가 R2에 좋아요·저장, B가 R1에 좋아요·저장
- A가 다른 레시피 R3를 신고, C가 R1을 신고
- A의 알림, 알림 설정, FCM 토큰, 약관 동의 이력, AI 피드백
- A의 1:1 문의와 첨부 이미지

## SC-01 레시피를 남기는 경우
When: A가 "내 레시피도 함께 삭제"를 고르지 않고, 탈퇴 사유를 적고 탈퇴한다.

Then:
- `users` 행은 남고 `status = WITHDRAWN`, 이메일·닉네임은 NULL (C-02)
- A의 `social_accounts` 행이 없음 (U-06)
- R1은 `ACTIVE`로 남고, 조회하면 작성자가 "탈퇴한 사용자" (U-05)
- R2의 `like_count`, `save_count`가 각각 1 줄고 A의 좋아요·저장 행이 없음 (U-06, DEL-03)
- B가 R1에 누른 좋아요·저장은 그대로 (R-07)
- A의 AI 피드백은 남고 `user_id`만 NULL (U-06)
- A의 약관 동의 이력, 알림, 알림 설정, FCM 토큰, 문의가 없음 (U-06)
- A가 한 신고는 남고 `reporter_id`가 NULL (U-06)
- C가 R1에 한 신고는 계속 처리 대상(`RECEIVED`) (U-06)
- 탈퇴 사유는 사용자와 연결되지 않은 채로 저장 (U-06)
- 커밋 후 `FakeFileStorage`에 A의 문의 이미지 키가 기록되고, `FakeAppleTokenRevoker`가 1회 호출됨 (U-06, DEL-04)

같은 클래스의 변형:
- `[A-03]` 탈퇴 후 A의 기존 토큰으로 요청하면 `UNAUTHORIZED` (HTTP 진입)
- `[U-05]` 작성자 검색에서 A가 나오지 않음
- `[U-02][C-02]` A가 쓰던 닉네임으로 새 회원이 바로 가입할 수 있음
- `[U-07]` 같은 소셜 계정으로 다시 가입하면 새 회원(새 `users.id`)
- `[U-05]` A가 남긴 R1은 누구도 수정·삭제할 수 없음
- `[DEL-04]` `FakeFileStorage`가 예외를 던져도 탈퇴는 성공
- `[U-06]` 탈퇴 처리 중 DB 단계에서 예외가 나면 모든 데이터가 그대로이고 외부 호출 기록이 0건

## SC-02 레시피도 삭제하는 경우
When: A가 "내 레시피도 함께 삭제"를 고르고 탈퇴한다.

Then:
- R1이 SC-03의 삭제 연쇄를 그대로 탐: `DELETED`, 좋아요·저장 삭제, C의 신고 `TARGET_DELETED` (U-05, R-07, 정책 9장)
- B의 저장 목록에서 R1이 사라짐 (R-07)
- 나머지 Then은 SC-01과 같음

SC-01과 SC-02는 `@ParameterizedTest`로 합치지 않고 따로 둡니다. 두 기대값의 차이가 이 시나리오의 핵심입니다.

## 결정 대기
- U-06 AI 피드백 "기타" 자유 입력의 개인정보 여부. 개인정보로 보면 SC-01의 AI 피드백 줄이 "기타 입력도 비움"으로 바뀝니다.
- U-06 약관 동의 이력 즉시 삭제 (법무 확인 필요)
- A-01 토큰 발급 주체, Apple 토큰 철회 위치. Firebase Auth를 쓰면 Firebase 사용자 삭제 호출이 Then에 추가될 수 있습니다.
- U-05 체크박스 기본값. 탈퇴 API 계약 테스트의 기본 요청이 달라집니다.
