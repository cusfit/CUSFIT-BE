# 코드 컨벤션

이 문서는 프로젝트가 아직 스켈레톤 단계(`com.cusfit.cusfitbe`에 코드가 거의 없는 상태)일 때, 앞으로 코드를 작성하면서 지킬 관례를 미리 정리한 것입니다.

일부 항목은 유사한 구조를 가진 참고 프로젝트(Deoham-BE)의 실제 사례를 참고해 정리했습니다. 이 저장소(CUSFIT-BE)에는 아직 해당 파일이 존재하지 않으므로, 아래 "참고 사례"는 근거가 아니라 예시로만 읽으세요. 실제로 코드를 작성하면 그 예시를 우리 저장소의 실제 경로로 교체해 나가야 합니다.

패키지 레이아웃, 응답 포맷, 인증 흐름 등 더 큰 아키텍처 규칙을 담을 `CLAUDE.md`는 아직 이 저장소에 없습니다. 여기서는 그런 상위 아키텍처가 아니라 코드 레벨 네이밍/배치/테스트 관례만 다룹니다.

---

## 1. 네이밍 규칙

### 1.1 Service 인터페이스는 Read/Write로 분리한다

읽기 전용 조회와 상태 변경 로직을 별도 인터페이스로 분리하는 것이 기본 패턴입니다.

- 참고 사례 (Deoham-BE): `card/service/CardReadService.java`, `card/service/CardWriteService.java` — `CardReadService`(조회), `CardWriteService`(생성/취소/재시도 등)로 분리.
- 참고 사례 (Deoham-BE): `user/service/UserReadService.java`, `user/service/UserWriteService.java` — 동일 패턴이 `user` 패키지에도 적용됨.

### 1.2 구현체는 `impl` 하위 패키지 + `~Impl` 접미사로 통일한다

인터페이스와 구현을 패키지로도 분리해 인터페이스 패키지만 봤을 때 계약이 한눈에 보이도록 합니다. 이 방식은 3.7의 다중 구현체(Failover 등) 패턴과도 자연스럽게 맞물립니다 — 구현체가 여러 개여도 `impl` 패키지 안에서 이름만 다르게 나열하면 됩니다.

- 예: `card/service/CardReadService`(인터페이스) / `card/service/impl/CardReadServiceImpl`(구현).
- 주의 (Deoham-BE 반례): 참고 프로젝트는 `Default~`(인터페이스와 같은 패키지)와 `~Impl`(impl 하위 패키지)이 도메인마다 혼재해 있었습니다. 우리는 시작 단계이므로 이런 비일관성을 이식하지 않고 `impl` 하위 패키지 방식으로 통일합니다.

### 1.3 DTO는 `dto/request`, `dto/response` 하위 패키지로 분리한다

요청 바디는 `~Request`, 응답 바디는 `~Response`로 끝나며, 모든 도메인에서 `dto/request`, `dto/response` 하위 패키지로 물리적으로 분리합니다. 패키지 수가 늘어나는 비용보다 IDE 탐색이 쉬워지는 이득이 큽니다.

- 예: `card/dto/request/CreateCardRequest.java`, `card/dto/response/CardDetailResponse.java`.
- 주의 (Deoham-BE 반례): 참고 프로젝트는 `card`만 하위 패키지로 분리하고 `chat`은 같은 `dto` 패키지에 이름으로만 구분하는 등 도메인마다 달랐습니다. 우리는 처음부터 모든 도메인에 동일하게 적용합니다.

### 1.4 Controller는 `~Controller`, Swagger 문서는 `controller/docs`의 `~ControllerDocs` 인터페이스로 분리한다

`@Operation`, `@ApiResponse` 등 Swagger 애너테이션은 컨트롤러 구현체가 아니라 별도 인터페이스에 선언하고, 컨트롤러가 이를 `implements`합니다.

- 참고 사례 (Deoham-BE): `card/controller/docs/CardControllerDocs.java` — `public interface CardControllerDocs { @Operation(...) ResponseEntity<CardDetailResponse> createCard(...); }`
- 참고 사례 (Deoham-BE): `card/controller/CardController.java` — `public class CardController implements CardControllerDocs` — 실제 구현에는 `@Override`만 있고 Swagger 애너테이션은 없음.

### 1.5 엔티티의 상태 변경 메서드는 동사 접두사로 의도를 드러낸다

세터(setter) 대신 `update~`(필드 값 교체), `increment~`(카운터 증가), `mark~`(플래그/이벤트성 상태 전환) 패턴을 사용합니다.

- 참고 사례 (Deoham-BE): `card/entity/Card.java` — `updateStatus(CardStatus status)`, `incrementRetryCount()`, `updateExpiresAt(Instant expiresAt)`.
- 참고 사례 (Deoham-BE): `user/entity/User.java` — `incrementHelpCount()`, `incrementHelpRequestCount()`, `updateProfile(...)`, `markCardCreated()`, `markCardViewOnboardingSeen()`.

### 1.6 조회 실패를 던지는 리포지토리 헬퍼는 `~OrThrow` 접미사를 쓴다

리포지토리를 감싸 "찾거나 예외" 로직을 제공하는 메서드는 `find~OrThrow` 형태로 명명합니다.

- 참고 사례 (Deoham-BE): `chat/service/ChatAccessGuard.java` — `findMessageOrThrow(UUID messageId)`, `findRoomOrThrow(UUID roomId)`.

### 1.7 Spring Data 리포지토리 메서드는 쿼리 메서드 규약을 그대로 사용한다

커스텀 `@Query` 없이도 표현 가능한 조회는 `find/exists/delete + By + 조건` 형태의 메서드명을 사용합니다.

- 참고 사례 (Deoham-BE): `card/repository/CardRepository.java` — `findByRequesterAndStatusIn(...)`, `findFirstByRequesterIdAndStatusIn(...)`, `findByStatusInAndExpiresAtBefore(...)`.

---

## 2. 정책/검증 코드의 배치 기준

### 2.1 단일 서비스 내부에서만 쓰이는 검증은 `private` 메서드로 둔다

특정 서비스의 여러 public 메서드에서 반복되지만 다른 도메인/클래스에서 재사용되지 않는 검증은 같은 서비스 클래스의 `private` 메서드로 추출합니다.

- 참고 사례 (Deoham-BE): `card/service/impl/CardWriteServiceImpl.java` — `private Card findCardAndValidateOwner(UUID cardId, UUID userId)`가 `cancelCard`, `completeCard`, `retryCard` 3곳에서 재사용되지만 해당 클래스 내부에만 존재.
- 대비: 상태 전이 검증처럼 메서드마다 조건이 다른 경우는 별도 추출 없이 각 public 메서드 내부에 인라인으로 남긴다 — 반복되는 "판별 로직"만 메서드로 뽑고, 메서드별로 다른 1회성 조건은 인라인 유지.

### 2.2 여러 서비스/컨트롤러 경계를 넘어 재사용되는 접근 제어는 별도 `@Component`(Guard/AccessService)로 분리한다 — 단, 로직 중복은 회피 수단이 아니라 최후의 수단이다

순환 의존을 끊기 위해 검증 로직을 별도 컴포넌트로 승격하는 것 자체는 유효한 선택입니다. 하지만 "같은 검증 로직을 여러 클래스에 그대로 복제"하는 것은 해법이 아니라 문제 회피입니다. 순환 의존이 실제로 발생하면 다음을 먼저 시도합니다.

1. 공유 로직을 의존성이 없는 별도 컴포넌트/인터페이스로 추출해 양쪽이 그것을 함께 의존하게 한다.
2. 모듈 경계 자체를 재설계해 순환이 생기지 않게 한다.

로직 중복은 위 두 방법이 정말 불가능할 때만 최후의 수단으로 남깁니다.

- 주의 사례 (Deoham-BE 반례): `chat/service/ChatAccessGuard.java`와 `chat/service/ChatRoomAccessService.java`에 `requireParticipant(Card card, UUID userId)`가 순환 의존 회피를 위해 동일 로직으로 각각 존재합니다. 이건 "권장 패턴"이 아니라 반례로만 기억할 것 — 새 접근 제어 로직을 추가할 때 같은 상황이면 위 1·2번을 먼저 시도합니다.

### 2.3 도메인 예외는 공통 `BusinessException` + `ErrorCode` enum으로 표현하고, 메시지는 호출부에서 문자열로 전달한다

`ErrorCode`는 HTTP 상태별로 소수의 범용 코드만 정의하고, 구체적인 실패 사유는 `BusinessException` 생성 시 두 번째 인자(message)로 넘깁니다. "정책 위반 종류"를 늘릴 때 `ErrorCode`에 항목을 추가하기보다 기존 코드 + 메시지 조합을 우선 사용합니다.

- 참고 사례 (Deoham-BE): `global/exception/ErrorCode.java` — `INVALID_REQUEST`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_ERROR` 6종만 존재.
- 참고 사례 (Deoham-BE): `global/exception/BusinessException.java` — `public BusinessException(ErrorCode errorCode, String message)` 생성자가 이 패턴을 위해 명시적으로 제공됨.
- 한계: 메시지가 한국어 문자열로 하드코딩되므로 다국어(i18n) 지원 계획이 생기면 이 정책은 재검토 대상입니다. 지금 규모에서는 유지하되, 메시지 문자열에 로직을 태우지 않습니다(예: 메시지 파싱으로 분기하지 않기).

### 2.4 트랜잭션 스코프 안에서만 유효한 값은 별도 record로 "탈출"시켜 비트랜잭션 계층에 넘긴다

`open-in-view: false`로 설정하면 트랜잭션 밖에서는 지연 로딩(lazy loading)이 불가능합니다. 트랜잭션 밖의 계층(비동기 처리, 다른 스레드로 넘어가는 이벤트 리스너 등)에서 필요한 값은, 트랜잭션 메서드 안에서 미리 순수 값 객체(record)로 변환해 반환합니다.

- 참고 사례 (Deoham-BE): `chat/service/ChatRoomAccessService.java` — `cardTargetLocation(UUID roomId)`에서 "비트랜잭션 계층에 lazy 엔티티를 넘기지 않고 트랜잭션 경계 안에서 순수 좌표만 뽑아 `record CardTargetLocation(double latitude, double longitude)`로 변환 후 반환.
- 참고 사례 (Deoham-BE): `notification/event/FcmPushEvent.java` — 엔티티가 아니라 원시값만 담아 트랜잭션 커밋 이후(다른 스레드)에도 detached/lazy 로딩 문제가 없게 함.

---

## 3. 기타 반복되는 구조적 관례

### 3.1 DTO ↔ Entity 변환은 DTO의 정적 팩토리 메서드(`from`/`of`)로 통일한다

DTO가 자신을 채우는 방법을 스스로 캡슐화하면 서비스 코드가 필드를 일일이 나열하지 않아도 되고, 변환 로직이 한 곳에 모입니다. 테스트에서 픽스처를 만들 때도 같은 팩토리를 재사용할 수 있어 4.3(Fixture 패턴)과도 연결됩니다.

- 예: `XxxResponse.from(entity)`, `XxxResponse.of(a, b)`.
- 주의 (Deoham-BE 반례): 참고 프로젝트는 일부 도메인(`user`, `report`, `notification`)만 정적 팩토리를 쓰고 `card`는 서비스 메서드 안에서 생성자를 14개 필드까지 직접 나열하는 방식이 혼재했습니다. 필드가 늘어날수록 가독성과 테스트 유지보수가 나빠지므로, 우리는 정적 팩토리로 통일합니다.

### 3.2 컨트롤러는 리포지토리를 직접 참조하지 않고 항상 서비스 계층을 통한다

컨트롤러 → 서비스 → 리포지토리 순서를 예외 없이 지킵니다. 컨트롤러 코드에서 `*.repository.*` import가 보이면 리뷰에서 반려합니다.

### 3.3 목록/집계성 조회는 Spring Data 인터페이스 프로젝션을 사용한다

단순 엔티티 반환이 아니라 일부 컬럼만 필요한 집계 쿼리는 `~Projection` 인터페이스로 결과를 매핑합니다.

- 참고 사례 (Deoham-BE): `chat/repository/UnreadCountProjection.java` — `interface UnreadCountProjection { UUID getRoomId(); Long getUnreadCount(); }`

### 3.4 주기 실행 로직은 `~Scheduler` 컴포넌트로 분리하고, 실제 상태 변경은 반드시 Write 서비스에 위임한다

`@Scheduled` 메서드 자신은 엔티티나 리포지토리를 직접 다루지 않고, 트랜잭션 처리는 서비스로 위임한 뒤 예외를 로깅만 하고 삼킵니다(스케줄러 스레드가 죽지 않도록).

- 참고 사례 (Deoham-BE): `card/scheduler/CardScheduler.java` — `@Scheduled(fixedDelay = 60000) public void expireCards() { try { cardWriteService.expireCards(); } catch (Exception e) { log.error(...); } }`

### 3.5 부수 효과는 이벤트 발행으로 분리하고, 커밋 이후 실행이 필요하면 `@TransactionalEventListener`를 쓴다

알림 발송처럼 트랜잭션이 성공적으로 커밋된 후에만 실행돼야 하는 부수효과는 서비스가 직접 호출하지 않고 이벤트를 발행한 뒤 별도 리스너가 처리합니다. 이때 일반 `@EventListener`는 트랜잭션 커밋 전에도 동기 실행되므로, 커밋 후 실행이 필요하면 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`을 명시적으로 사용합니다. 그냥 `@EventListener`를 쓰면 트랜잭션이 롤백돼도 이미 부수효과(알림 등)가 실행돼버리는 문제가 생깁니다.

- 참고 사례 (Deoham-BE): `notification/service/NotificationService.java` — 여러 알림 시점에서 `eventPublisher.publishEvent(new FcmPushEvent(...))` 호출, `notification/service/FcmSender.java`에서 `@Async("fcmTaskExecutor")`로 실제 발송은 별도 스레드에서 비동기 처리.

### 3.6 외부 연동 설정은 `~Config` + `~Properties` 쌍으로 분리한다

외부 시스템(S3, DeepL, Gemini 등) 연동은 `@ConfigurationProperties`를 붙인 불변 `record ~Properties`와, `@EnableConfigurationProperties`로 이를 활성화하며 `@Bean`을 만드는 `~Config` 클래스로 항상 쌍을 이룹니다.

- 참고 사례 (Deoham-BE): `global/config/S3Properties.java` — `@ConfigurationProperties(prefix = "deoham.s3") public record S3Properties(...)` / `global/config/S3Config.java` — `@EnableConfigurationProperties(S3Properties.class) public class S3Config { @Bean ... }`

### 3.7 다중 프로바이더 페일오버가 실제로 필요할 때만 인터페이스 + 여러 구현체 + Failover/Circuit-breaker 래퍼를 쓴다

인터페이스를 두고 우선순위가 다른 구현체를 여러 개 두고, 이를 감싸는 Failover 구현체가 서킷 브레이커로 전환을 판단하는 구조입니다. 이 패턴은 오버엔지니어링 비용이 크므로 "외부 연동은 무조건 이렇게"가 아니라, 대체 가능한 공급자가 실제로 여럿 있는 경우(예: 번역 API처럼 공급사가 여러 곳)에만 적용합니다. S3처럼 단일 프로바이더만 쓰는 연동에는 이 구조를 강제하지 않습니다.

- 참고 사례 (Deoham-BE): `chat/translation/TranslationProvider.java`(인터페이스) — 구현체 `DeepLTranslationProvider`, `GeminiTranslationProvider`, `DummyTranslationProvider`를 `FailoverTranslationProvider` + `SimpleCircuitBreaker`가 감쌈.

---

## 4. 테스트(TDD) 규칙

### 4.1 테스트 종류별 경계를 명확히 나눈다

- **Service 단위 테스트**: Repository를 Mockito로 목킹하고, 컨테이너 없이 순수 단위 테스트로 작성한다.
- **Repository 테스트**: `@DataJpaTest` + Testcontainers(PostgreSQL)로 실제 쿼리 동작만 검증한다.
- **Controller 테스트**: `@WebMvcTest` + MockMvc, Service 계층은 목킹한다. (`~ControllerDocs`의 Swagger 애너테이션은 테스트 대상이 아니다.)
- **통합 테스트**: `@SpringBootTest` + Testcontainers는 여러 컴포넌트가 함께 동작하는 흐름(예: 이벤트 발행 → 리스너 처리)을 검증할 때만 사용한다.

TDD의 Red-Green-Refactor 사이클은 피드백이 빨라야 의미가 있습니다. 모든 테스트가 컨테이너를 띄우면 한 사이클에 수십 초가 걸려 사이클 자체가 느려지므로, 대부분의 테스트는 컨테이너 없는 단위 테스트로 두고 컨테이너가 필요한 테스트는 최소화합니다.

### 4.2 Given-When-Then 구조와 한글 `@DisplayName`을 사용한다

테스트를 실행 가능한 스펙 문서로 취급합니다. `@DisplayName("~하면 ~한다")` 형태로 한글로 작성하고, 테스트 본문은 given/when/then 세 구간으로 나눠 별도 설명 주석 없이도 흐름이 읽히게 작성합니다.

### 4.3 Fixture/Test Data Builder로 테스트 데이터를 생성한다

엔티티 필드가 늘어날수록 생성자를 직접 호출하는 테스트는 필드 추가마다 전부 깨집니다. 도메인별로 Fixture(또는 Builder)를 두고, 테스트는 검증에 필요한 필드만 지정하고 나머지는 기본값으로 채웁니다. (3.1에서 정적 팩토리로 통일한 것과 같은 이유 — 생성 로직을 한 곳에 모아둔다.)

### 4.4 실패 케이스는 `assertThrows(BusinessException.class)` + `ErrorCode` 검증으로 표준화한다

`~OrThrow`/`BusinessException` 패턴(1.6, 2.3)을 쓰기로 했으므로, 예외 테스트도 예외 타입과 `ErrorCode`만 검증합니다. 메시지 문자열은 assert하지 않습니다 — 2.3에서 메시지는 다국어 대응 시 바뀔 수 있는 값으로 남겨뒀기 때문입니다.

### 4.5 다중 구현체 인터페이스는 계약 테스트(Contract Test)를 공유한다

3.7에서 여러 구현체 + Failover 구조를 적용하기로 결정한 인터페이스에 한해, 인터페이스 레벨의 공통 테스트 스윗을 두고 모든 구현체가 그것을 통과하게 합니다. 새 구현체를 추가하거나 기존 구현체를 교체해도 Failover 로직이 깨지지 않는다는 것을 보장합니다.

---

## 요약

| 구분 | 문서화한 규칙 수 |
|---|---|
| 1. 네이밍 규칙 | 7 |
| 2. 정책/검증 코드 배치 기준 | 4 |
| 3. 기타 구조적 관례 | 7 |
| 4. 테스트(TDD) 규칙 | 5 |
| **합계** | **23** |
