# 작업: scenario-ai-analysis — SC-09 AI 분석 횟수와 AI 피드백

## 배정
- 작업 ID: scenario-ai-analysis
- 선행 작업: scenario-infra(Redis 컨테이너, `MutableClock`, `FakeAiAnalyzer`), recipe AI 분석·AI 피드백
- 테스트 클래스: `scenario/AiAnalysisScenarioTest`
- 검증 대상: `'*AiAnalysisScenarioTest'`

## 선행 기능 (이름은 기능 작업에서 확정)
- AI 분석: DB 트랜잭션 없이 실행, 결과는 저장하지 않고 응답으로만 돌려줌 (CON-05, R-10)
- 일일 횟수 제한 20회, Redis `INCR` + TTL, 날짜 경계는 KST (R-03, CON-05, D-03). 초과 시 `TOO_MANY_REQUESTS`
- AI 피드백: AI로 만든 레시피 1건당 1회, 사유 여러 개, AI 원본 결과 JSON(`jsonb`) 함께 저장 (R-11, OPS-02)
- 날짜 경계 계산은 코드 한 곳에서 `Clock` 빈을 사용 (D-03)

## 시나리오
- `[R-03]` 하루 `LIMIT`회까지는 성공, `LIMIT + 1`번째는 `TOO_MANY_REQUESTS`
- `[D-03]` KST 23:59에 `LIMIT`회를 채우고, 시계를 KST 00:00으로 옮기면 다시 성공
- `[R-03]` 다른 사용자의 횟수와 섞이지 않음
- `[R-10][CON-05]` 분석 결과는 DB에 저장되지 않고, 업로드한 이미지는 어디에도 남지 않음 (`FakeFileStorage` 업로드 기록 0건, 관련 테이블 행 0개)
- `[R-11]` AI로 만든 레시피 1건에 피드백 1회. 두 번째는 `CONFLICT`
- `[R-11][OPS-02]` 피드백에 AI 원본 결과 JSON이 함께 저장됨
- `[R-11]` 직접 만든 레시피에는 피드백을 받지 않음
- `[R-11]` 싫어요 사유를 여러 개 저장할 수 있음
- `[R-11]` "기타" 직접 입력은 200자까지

## 결정 대기
- AI 호출이 실패했을 때 횟수를 차감할지 문서에 없습니다.
- R-03 숫자(20회)는 출시 후 조정. 테스트는 상수를 참조합니다.
- AI 피드백을 남기지 않은 "나중에 할게요"를 기록할지 (R-11은 "다시 묻지 않음"만 적음)
- `TOO_MANY_REQUESTS` `ErrorCode` 추가 (SC-06과 같음)
