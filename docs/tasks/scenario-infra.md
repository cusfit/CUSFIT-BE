# 작업: scenario-infra — 시나리오·동시성 테스트 공용 도구

## 배정
- 작업 ID: scenario-infra
- 선행 작업: 없음
- 작성자 허용 경로: 없음 (테스트 인프라. 관리자 또는 검증자가 `src/test/**`에 작성). 단, `Clock` 빈과 외부 연동 인터페이스는 `src/main/**` 변경이 필요해 해당 기능 작업에서 같이 만든다.
- 검증 대상: `'*ScenarioInfrastructureTest'`

## 완료된 것 (`leesuyong849/scenario-tests`)
- `testsupport/ScenarioTest` — `@SpringBootTest` + `PostgisContainerConfig` + `ScenarioTestConfig` + `DatabaseCleanupExtension`
- `testsupport/DatabaseCleaner` — `public` 스키마 테이블을 `TRUNCATE ... RESTART IDENTITY CASCADE`. `flyway_schema_history`, `spatial_ref_sys` 제외
- `testsupport/DatabaseCleanupExtension` — 각 테스트 후 `DatabaseCleaner.clean()`
- `scenario/ScenarioInfrastructureTest` — 서비스로 만든 데이터가 커밋되고 정리 후 비는지 확인

공용 도구 패키지는 가이드의 `support/` 대신 `testsupport/`입니다. `support`는 고객지원 도메인 패키지 이름이라 겹칩니다.

## 남은 것

| 도구 | 위치 | 필요해지는 시점 | 내용 |
|---|---|---|---|
| Redis 컨테이너 | `PostgisContainerConfig` 옆 또는 `testsupport/container` | SC-09 (AI 분석 횟수, CON-05 `INCR`+TTL) | `GenericContainer` `redis:7-alpine`, `@ServiceConnection(name = "redis")`. `DatabaseCleaner`가 Redis도 비움 |
| `MutableClock` | `testsupport` | SC-09, SC-10 | `setKst("2026-01-01T23:59")`, `plus(Duration)`. 운영 코드가 `Clock` 빈과 `Instant.now(clock)`을 써야 함. `BaseCreatedEntity.@PrePersist`도 같은 시계를 따르게 해야 함 |
| 기록형 Fake | `testsupport/fake` | SC-01, SC-05, SC-09 | `FakeFileStorage`(삭제 키 기록, `failNextDelete()`), `FakePushSender`(발송 기록), `FakeAppleTokenRevoker`(호출 수), `FakeAiAnalyzer`(호출 수, 고정 결과). 모두 `reset()`을 두고 `DatabaseCleanupExtension`에서 함께 초기화 |
| `FakeExternalConfig` | `testsupport` | Fake가 생길 때 | 모든 Fake를 한 설정에 모아 `@ScenarioTest`에 `@Import`. 개별 테스트가 `@MockitoBean`을 추가하면 컨텍스트 캐시가 깨지므로 금지 |
| `ScenarioSteps` | `testsupport` | SC-01 이후 | `회원가입(nickname)`, `레시피작성(authorId)`, `좋아요`, `저장`, `신고`, `문의작성` 등. 테스트 본문이 정책 문장처럼 읽히게 함 |
| `ConcurrencyRunner` | `testsupport/concurrency` | 동시성 테스트 | `CountDownLatch`로 동시 출발, 성공 수와 예외 종류별 수를 돌려줌. 스레드 수는 커넥션 풀(기본 10) 안에서 |
| `application-test.yml` | `src/test/resources` | 외부 설정이 컨텍스트 로딩을 막을 때 | Loki, Firebase 자격 증명 경로 등. 현재는 없어도 `contextLoads`가 통과함 |
| JUnit 태그 | `@ScenarioTest`에 `@Tag("scenario")`, 동시성에 `@Tag("concurrency")` | 시나리오가 느려질 때 | `build.gradle`에 `fastTest` 태스크(선택). CI와 머지 전에는 전체 실행(컨벤션 5.1) |

## 미결정 / 가정
- **Fake를 위한 인터페이스**: Fake를 끼우려면 S3·FCM·Apple·AI 호출이 인터페이스 뒤에 있어야 합니다. 컨벤션 3.7("인터페이스 + 여러 구현체는 공급자가 실제로 여럿일 때만")과 5.4(미래 확장용 추상화 경계)에 걸리는지 팀 결정이 필요합니다. 인터페이스를 두지 않으면 모든 시나리오 테스트가 같은 `@MockitoBean` 목록을 공유하게 합니다.
- **비동기 리스너**: FCM 발송을 `@Async`로 하면 테스트 프로필에서 `SyncTaskExecutor`로 바꿀지, Awaitility를 추가할지 정해야 합니다. 현재 Awaitility 의존성은 없습니다.
- **`-PcusfitBuildDir` 주의**: 빌드 폴더를 바꾸면 ArchUnit `DO_NOT_INCLUDE_TESTS`가 테스트 클래스를 구분하지 못해 `ArchitectureTest`가 실패합니다(경로 패턴 `build/classes/java/test` 기준).
