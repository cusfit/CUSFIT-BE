# 작업: scenario-signup-auth — SC-08 가입과 인증 상태

## 배정
- 작업 ID: scenario-signup-auth
- 선행 작업: scenario-infra, 운영 보안 설정(JWT 리소스 서버), user 가입 완료·소셜 계정, terms 약관 동의 이력
- 테스트 클래스: `scenario/SignupAuthScenarioTest`
- 검증 대상: `'*SignupAuthScenarioTest'`
- 진입점: HTTP(MockMvc, 전체 컨텍스트). 인증과 응답 포맷을 함께 봐야 하는 시나리오입니다.

## 선행 기능 (이름은 기능 작업에서 확정)
- 토큰 `sub` → `users.auth_uid` → `userId` 변환 (A-02)
- 가입 완료 API: 닉네임, 필수 약관 동의, 만 14세 체크 (U-02, U-03, T-01)
- 가입 완료 전에는 가입 완료 API만 허용, 나머지는 `UNAUTHORIZED` (A-02)
- `status = WITHDRAWN`이면 거부 (A-03)
- `UNIQUE (lower(nickname))` (U-02, CON-02), `social_accounts (provider, provider_user_id)` 유니크 (U-01)
- 약관 `(type, version, effective_at)`, 동의 이력은 약관 버전 ID를 가리킴 (T-01)

현재 운영 보안 설정은 없고 `local` 프로필에서 `/api/dev/**`만 열려 있습니다. 이 시나리오는 운영 보안 설정이 생긴 뒤 씁니다.

## 시나리오
- `[A-02]` 토큰은 유효하지만 가입을 마치지 않은 사용자는 가입 완료 API만 쓸 수 있고, 다른 API는 `UNAUTHORIZED`
- `[A-02]` 가입을 마치면 같은 토큰으로 다른 API를 쓸 수 있음
- `[T-01]` 가입 시 약관 동의 이력이 약관 버전 ID를 가리킴
- `[U-03]` 만 14세 체크가 없으면 가입 거부
- `[U-02]` `Ondo`가 있으면 `ondo`로 가입 거부 (`CONFLICT`)
- `[U-02]` 닉네임 길이(2~12자)·문자 종류(한글, 영문, 숫자, 공백 없음) 위반이면 `INVALID_REQUEST`
- `[U-02]` 운영 사칭 금칙어(운영자, 관리자, cusfit 등)는 거부
- `[U-01]` 같은 이메일로 Apple·구글 각각 가입하면 별개 회원
- `[T-01]` 필수 약관 새 버전이 시행되면 기존 회원은 재동의 필요 상태
- `[A-03]` 탈퇴한 사용자의 토큰은 `UNAUTHORIZED` (SC-01 변형과 같은 규칙. 한쪽에만 둠)
- 모든 실패 응답이 `ApiResponse` 형식(`success: false`, `error.code`)

## 결정 대기
- **A-01 토큰 발급 주체(Firebase 또는 자체 JWT)**: 정해지기 전까지 테스트는 `spring-security-test`의 `jwt()`로 토큰을 흉내 내고, `sub` → `users.id` 변환만 실제 코드로 돌립니다.
- A-02 가입 전 상태를 `PENDING` 행으로 둘지, 가입 완료 시점에 행을 만들지 (추천은 후자)
- U-02 금칙어 목록, 대소문자 무시 판정
- U-03 법무 확인
- T-01 재동의 화면이 Figma에 없음
