# 시나리오 테스트 개요

> 등록용 작업 스펙이 아니라 `scenario-*` 작업 스펙의 색인입니다. 원천은 Artifact "커스핏 백엔드 테스트 가이드"(2026-10-28, `d43f70b` 기준)의 4·5·9·12장이고, 기대값은 `docs/BUSINESS_POLICY.md`, `docs/TECH_DECISIONS.md`의 추천안입니다. 추천안이 확정되지 않았으므로 각 문서의 "결정 대기"가 풀리면 기대값을 고칩니다.

## ID 규칙

- 시나리오 ID는 `SC-xx`입니다. 가이드의 `S-xx`와 같은 번호이며, 정책 문서의 support 항목(`S-01`~`S-03`)과 겹치지 않게 바꿨습니다.
- 테스트의 `@DisplayName` 앞에는 시나리오가 아니라 **정책 ID**를 붙입니다. 예: `[U-05][U-06] 레시피 삭제를 고르지 않고 탈퇴하면 ...`. 정책이 바뀌면 `grep -r "\[R-04\]" src/test`로 영향받는 테스트를 찾습니다.

## 현재 상태 (`develop` `2797000` 기준)

모든 도메인에 `create`와 `getById`만 있습니다(`domain-basics-contract.md`). 아래 시나리오가 쓰는 기능은 아직 하나도 없으므로, 각 시나리오는 **선행 기능 작업이 integrated된 뒤** 발행합니다. 선행 기능 작업 스펙은 아직 없습니다.

| ID | 문서 | 내용 | 주요 정책 |
|---|---|---|---|
| — | `scenario-infra.md` | 공용 테스트 도구 | 가이드 9장 |
| SC-01, SC-02 | `scenario-withdrawal.md` | 회원 탈퇴 (레시피 남김 / 함께 삭제) | U-04~U-07, C-02, DEL-01, DEL-03, DEL-04, A-03 |
| SC-03 | `scenario-recipe-deletion.md` | 레시피 하나가 사라질 때 연쇄 동작 | 정책 9장, C-01, R-07, R-09, DEL-02, N-04 |
| SC-04 | `scenario-report-blind.md` | 신고 누적과 자동 블라인드 | RP-01~RP-05, N-01 |
| SC-05 | `scenario-reaction-milestone.md` | 좋아요·저장과 마일스톤 알림 | R-06, R-08, CON-03, CON-06, N-01, N-02, C-04 |
| SC-06 | `scenario-recipe-create.md` | 레시피 작성과 중복 판정 | R-02~R-04, CA-02~CA-04, DEL-02, API-03 |
| SC-07 | `scenario-recipe-update.md` | 레시피 수정 | R-05, R-04, U-05, CON-01 |
| SC-08 | `scenario-signup-auth.md` | 가입과 인증 상태 | A-01~A-03, U-01~U-03, T-01 |
| SC-09 | `scenario-ai-analysis.md` | AI 분석 횟수와 AI 피드백 | R-03, R-10, R-11, CON-05, D-03, OPS-02 |
| SC-10 | `scenario-cleanup-batch.md` | 정리 배치 | C-01, N-04, D-05, D-06, OPS-04 |
| CON | `scenario-concurrency.md` | 동시성 테스트 | CON-01~CON-03, CON-06, RP-04, DEL-03 |

## 발행 순서 (가이드 12장)

1. `scenario-infra` (일부 완료)
2. user 파일럿 기능 → SC-08
3. recipe 엔티티 기능 → SC-06, SC-07, 동시성 CON-01·CON-02
4. 좋아요·저장, notification 기능 → SC-05, 동시성 CON-03·CON-06
5. report 기능 → SC-04, 동시성 RP-04
6. 탈퇴·삭제 기능 → SC-03, SC-01, SC-02, 동시성 DEL-03
7. AI 분석 → SC-09
8. 배치 → SC-10

도메인이 하나씩 생길 때마다 SC-01의 Then 줄을 하나씩 늘려 가는 방식도 됩니다(가이드 11장).

## 모든 시나리오 작업의 공통 규칙

- 테스트 클래스는 `src/test/java/com/cusfit/cusfitbe/scenario/`에 두고 `@ScenarioTest`를 붙입니다. 동시성 테스트도 같은 패키지에 둡니다.
- `@Transactional`을 붙이지 않습니다. 실제로 커밋해야 `@TransactionalEventListener(AFTER_COMMIT)`가 실행됩니다. 데이터는 `DatabaseCleaner`가 테스트마다 비웁니다.
- 진입점은 Write 서비스입니다. HTTP(MockMvc)는 SC-08처럼 인증·응답 포맷까지 봐야 하는 흐름에만 씁니다.
- Then에서는 DB에 남은 상태와 Fake에 기록된 외부 호출만 검증합니다. 내부 메서드 호출 검증은 서비스 단위 테스트(Mockito)가 맡습니다.
- Given은 공개 서비스로 만들 수 있으면 서비스로(좋아요는 `like`로 눌러야 카운트가 실제 경로로 오름), 비용이 크면 Fixture + Repository로 만듭니다.
- 시나리오마다 실패 경로를 하나 이상 넣습니다. DB 단계 예외 시 전부 롤백되고 외부 호출 기록 0건, 커밋 후 외부 호출 실패 시에도 요청은 성공(DEL-04).
- 정책 숫자(3명, 20회, 30개, 30일, 90일, 마일스톤 목록)는 운영 코드의 상수·설정값을 참조하고 `THRESHOLD - 1`, `THRESHOLD`로 경계를 씁니다.
- 선택지가 갈리는 기대값은 한 클래스에 모으고, 결정 전이면 `@Disabled("R-04 결정 대기")`로 둡니다.
- 실패 검증은 `BusinessException`과 `ErrorCode`만 봅니다(컨벤션 4.4).

## 파이프라인 적용

- 시나리오 작업은 테스트만 추가하므로 검증자(write-tests)가 `src/test/**`를 쓰고, 기능이 이미 있다면 Red 없이 Green으로 끝날 수 있습니다. 반대로 시나리오가 기능의 누락을 드러내면 해당 기능 작업으로 되돌립니다.
- `pipeline.py register`는 `--write-path`를 요구합니다. 시나리오 작업을 기능 작업과 함께 발행할지(작성자 허용 경로 = 기능 경로), 테스트 전용 작업으로 발행할지는 관리자가 정해야 합니다.
