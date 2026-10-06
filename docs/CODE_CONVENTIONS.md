# 코드 컨벤션

이 문서는 프로젝트가 아직 스켈레톤 단계(`com.cusfit.cusfitbe`에 코드가 거의 없는 상태)일 때, 앞으로 코드를 작성하면서 지킬 관례를 미리 정리한 것입니다.

일부 항목은 유사한 구조를 가진 참고 프로젝트(Deoham-BE)의 실제 사례를 참고해 정리했습니다. 이 저장소(CUSFIT-BE)에는 아직 해당 파일이 존재하지 않으므로, 아래 "참고 사례"는 근거가 아니라 예시로만 읽으세요. 실제로 코드를 작성하면 그 예시를 우리 저장소의 실제 경로로 교체해 나가야 합니다.

프로젝트 현황과 기술 스택, 아직 결정되지 않은 상위 아키텍처 항목은 루트의 `CLAUDE.md`에서 관리합니다. 이 문서는 그중 코드 레벨의 네이밍, 배치, 데이터베이스 변경, 테스트 관례를 다룹니다. 두 문서가 충돌하면 실제 코드와 설정을 확인한 뒤 두 문서를 같은 변경에서 함께 갱신합니다.

이 문서의 규칙은 기본값입니다. `반드시`, `금지`, `예외 없이`라고 명시한 항목은 필수 규칙이고, 그 밖의 항목은 합리적인 기본 선택입니다. 기본값을 벗어나는 편이 더 적절하다면 PR 설명에 이유와 대안을 남깁니다.

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

### 2.3 도메인 예외는 공통 `BusinessException` + 안정적인 `ErrorCode`로 표현한다

기본적으로 `ErrorCode`는 HTTP 상태별 범용 코드를 사용하고, 구체적인 실패 사유는 `BusinessException` 생성 시 메시지로 전달합니다. 단, 클라이언트가 실패 원인에 따라 동작을 달리해야 하거나 운영 지표에서 별도로 집계해야 하는 경우에는 도메인별 코드를 추가합니다. 즉, 사람이 읽는 설명만 다르면 범용 코드를 사용하고 기계가 구분해야 하는 계약이면 구체적인 코드를 사용합니다.

- 근거: `global/exception/ErrorCode.java` — `INVALID_REQUEST`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_ERROR` 6종만 존재 (Deoham-BE에서 그대로 포팅).
- 근거: `global/exception/BusinessException.java` — `public BusinessException(ErrorCode errorCode, String message)` 생성자가 이 패턴을 위해 명시적으로 제공됨.
- 근거: `global/response/ApiResponse.java`, `global/exception/GlobalExceptionHandler.java` — `@RestControllerAdvice`가 `BusinessException`을 `ApiResponse.fail(code, message)`로 변환.
- 메시지는 표시용이며 API 계약이나 분기 조건으로 사용하지 않습니다. 메시지 파싱으로 로직을 분기하지 않고, 다국어 지원이 필요해지면 메시지 생성 위치를 재검토합니다.

### 2.4 트랜잭션 스코프 안에서만 유효한 값은 별도 record로 "탈출"시켜 비트랜잭션 계층에 넘긴다

`open-in-view: false`로 설정하면 트랜잭션 밖에서는 지연 로딩(lazy loading)이 불가능합니다. 트랜잭션 밖의 계층(비동기 처리, 다른 스레드로 넘어가는 이벤트 리스너 등)에서 필요한 값은, 트랜잭션 메서드 안에서 미리 순수 값 객체(record)로 변환해 반환합니다.

- 참고 사례 (Deoham-BE): `chat/service/ChatRoomAccessService.java` — `cardTargetLocation(UUID roomId)`에서 "비트랜잭션 계층에 lazy 엔티티를 넘기지 않고 트랜잭션 경계 안에서 순수 좌표만 뽑아 `record CardTargetLocation(double latitude, double longitude)`로 변환 후 반환.
- 참고 사례 (Deoham-BE): `notification/event/FcmPushEvent.java` — 엔티티가 아니라 원시값만 담아 트랜잭션 커밋 이후(다른 스레드)에도 detached/lazy 로딩 문제가 없게 함.

---

## 3. 기타 반복되는 구조적 관례

### 3.1 응답 DTO 변환은 정적 팩토리 메서드(`from`/`of`)로 통일한다

응답 DTO가 자신을 채우는 방법을 스스로 캡슐화하면 서비스 코드가 필드를 일일이 나열하지 않아도 되고 변환 로직이 한 곳에 모입니다.

- 예: `XxxResponse.from(entity)`, `XxxResponse.of(a, b)`.
- 주의 (Deoham-BE 반례): 참고 프로젝트는 일부 도메인(`user`, `report`, `notification`)만 정적 팩토리를 쓰고 `card`는 서비스 메서드 안에서 생성자를 14개 필드까지 직접 나열하는 방식이 혼재했습니다. 필드가 늘어날수록 가독성과 테스트 유지보수가 나빠지므로, 우리는 정적 팩토리로 통일합니다.
- 요청 DTO는 HTTP 입력과 검증을 표현하는 객체입니다. 요청 DTO가 엔티티를 직접 변경하지 않으며, 엔티티 생성·변경은 서비스 또는 도메인 팩토리/메서드가 담당합니다.

### 3.2 컨트롤러는 리포지토리를 직접 참조하지 않고 항상 서비스 계층을 통한다

컨트롤러 → 서비스 → 리포지토리 순서를 예외 없이 지킵니다. 컨트롤러 코드에서 `*.repository.*` import가 보이면 리뷰에서 반려합니다.

### 3.3 단순 목록/집계 조회는 Spring Data 인터페이스 프로젝션을 우선 사용한다

일부 컬럼만 필요한 단순 목록·집계 쿼리는 `~Projection` 인터페이스로 결과를 매핑합니다. 복잡한 조인, 동적 조건, 공간 쿼리, 프로젝션으로 표현하기 어려운 페이징이 포함되면 DTO 쿼리나 별도 커스텀 리포지토리 구현을 사용할 수 있으며 선택 이유를 테스트나 PR에 남깁니다.

- 참고 사례 (Deoham-BE): `chat/repository/UnreadCountProjection.java` — `interface UnreadCountProjection { UUID getRoomId(); Long getUnreadCount(); }`

### 3.4 주기 실행 로직은 `~Scheduler` 컴포넌트로 분리하고, 실제 상태 변경은 반드시 Write 서비스에 위임한다

`@Scheduled` 메서드 자신은 엔티티나 리포지토리를 직접 다루지 않고 트랜잭션 처리를 서비스로 위임합니다. 예외는 스케줄러 실행 자체를 중단시키지 않도록 경계에서 처리하되, 반드시 스택 트레이스와 작업 식별자를 로깅합니다. 중요한 작업에는 실패 메트릭과 알림도 연결합니다.

스케줄 작업의 Write 서비스는 같은 대상을 다시 처리해도 안전하도록 멱등성을 고려합니다. 애플리케이션을 여러 인스턴스로 운영할 때 중복 실행이 문제가 된다면 분산 락이나 데이터베이스 기반 선점(`FOR UPDATE SKIP LOCKED` 등)을 적용합니다.

- 참고 사례 (Deoham-BE): `card/scheduler/CardScheduler.java` — `@Scheduled(fixedDelay = 60000) public void expireCards() { try { cardWriteService.expireCards(); } catch (Exception e) { log.error(...); } }`

### 3.5 부수 효과는 이벤트 발행으로 분리하고, 커밋 이후 실행이 필요하면 `@TransactionalEventListener`를 쓴다

알림 발송처럼 트랜잭션이 성공적으로 커밋된 후에만 실행돼야 하는 부수효과는 서비스가 직접 호출하지 않고 이벤트를 발행한 뒤 별도 리스너가 처리합니다. 이때 일반 `@EventListener`는 트랜잭션 커밋 전에도 동기 실행되므로, 커밋 후 실행이 필요하면 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`을 명시적으로 사용합니다. 그냥 `@EventListener`를 쓰면 트랜잭션이 롤백돼도 이미 부수효과(알림 등)가 실행돼버리는 문제가 생깁니다.

애플리케이션 내부 이벤트는 프로세스 종료 시 유실될 수 있으므로 전달 보장을 제공하지 않습니다. 결제, 필수 알림처럼 유실되면 안 되는 작업은 Outbox 같은 영속 이벤트 방식을 사용하고, 외부 호출은 재시도와 중복 실행을 고려해 멱등하게 설계합니다.

- 참고 사례 (Deoham-BE): `notification/service/NotificationService.java` — 여러 알림 시점에서 `eventPublisher.publishEvent(new FcmPushEvent(...))` 호출, `notification/service/FcmSender.java`에서 `@Async("fcmTaskExecutor")`로 실제 발송은 별도 스레드에서 비동기 처리.

### 3.6 외부 연동 설정은 `~Config` + `~Properties` 쌍으로 분리한다

외부 시스템(S3, DeepL, Gemini 등) 연동은 `@ConfigurationProperties`를 붙인 불변 `record ~Properties`와, `@EnableConfigurationProperties`로 이를 활성화하며 `@Bean`을 만드는 `~Config` 클래스로 항상 쌍을 이룹니다.

- 참고 사례 (Deoham-BE): `global/config/S3Properties.java` — `@ConfigurationProperties(prefix = "deoham.s3") public record S3Properties(...)` / `global/config/S3Config.java` — `@EnableConfigurationProperties(S3Properties.class) public class S3Config { @Bean ... }`

### 3.7 다중 프로바이더 페일오버가 실제로 필요할 때만 인터페이스 + 여러 구현체 + Failover/Circuit-breaker 래퍼를 쓴다

인터페이스를 두고 우선순위가 다른 구현체를 여러 개 두고, 이를 감싸는 Failover 구현체가 서킷 브레이커로 전환을 판단하는 구조입니다. 이 패턴은 오버엔지니어링 비용이 크므로 "외부 연동은 무조건 이렇게"가 아니라, 대체 가능한 공급자가 실제로 여럿 있는 경우(예: 번역 API처럼 공급사가 여러 곳)에만 적용합니다. S3처럼 단일 프로바이더만 쓰는 연동에는 이 구조를 강제하지 않습니다.

- 참고 사례 (Deoham-BE): `chat/translation/TranslationProvider.java`(인터페이스) — 구현체 `DeepLTranslationProvider`, `GeminiTranslationProvider`, `DummyTranslationProvider`를 `FailoverTranslationProvider` + `SimpleCircuitBreaker`가 감쌈.

### 3.8 엔티티/스키마 변경은 Flyway 마이그레이션과 함께 제출한다

이 프로젝트는 `spring.jpa.hibernate.ddl-auto: validate`를 사용하므로 Hibernate가 스키마를 생성하거나 변경하지 않습니다. 엔티티의 컬럼, 인덱스, 제약 조건을 변경하는 PR에는 대응하는 `src/main/resources/db/migration`의 Flyway 마이그레이션을 반드시 포함합니다.

- 운영에 적용된 마이그레이션 파일은 수정하거나 재사용하지 않고 새 버전 파일을 추가합니다.
- PostgreSQL/PostGIS 전용 타입, 인덱스, 네이티브 쿼리는 Testcontainers 기반 Repository 테스트로 실제 동작을 검증합니다.
- 파괴적 변경은 확장 → 데이터 이관 → 축소 단계로 나누어 구버전 애플리케이션과의 호환 구간을 확보합니다.

---

## 4. 테스트 규칙

### 4.1 테스트 종류별 경계를 명확히 나눈다

- **Service 단위 테스트**: Repository를 Mockito로 목킹하고, 컨테이너 없이 순수 단위 테스트로 작성한다.
- **Repository 테스트**: `@DataJpaTest` + Testcontainers(PostgreSQL)로 실제 쿼리 동작만 검증한다.
- **Controller 테스트**: `@WebMvcTest` + MockMvc, Service 계층은 목킹한다. (`~ControllerDocs`의 Swagger 애너테이션은 테스트 대상이 아니다.)
- **통합 테스트**: `@SpringBootTest` + Testcontainers는 여러 컴포넌트가 함께 동작하는 흐름(예: 이벤트 발행 → 리스너 처리)을 검증할 때만 사용한다.

위 네 경계를 지키는 이유 중 하나는 TDD의 Red-Green-Refactor 사이클이 빠르게 돌아야 의미가 있다는 점입니다 — 컨테이너가 필요한 테스트만 느리게 두고, 나머지는 컨테이너 없이 즉시 피드백을 받게 합니다. TDD를 어디에 어느 수준으로 적용하는지는 4.7에서 정합니다.

### 4.2 Given-When-Then 구조와 한글 `@DisplayName`을 사용한다

테스트를 실행 가능한 스펙 문서로 취급합니다. `@DisplayName("~하면 ~한다")` 형태로 한글로 작성하고, 테스트 본문은 given/when/then 세 구간으로 나눠 별도 설명 주석 없이도 흐름이 읽히게 작성합니다.

### 4.3 Fixture/Test Data Builder로 테스트 데이터를 생성한다

엔티티 필드가 늘어날수록 생성자를 직접 호출하는 테스트는 필드 추가마다 전부 깨집니다. 도메인별로 Fixture(또는 Builder)를 두고, 테스트는 검증에 필요한 필드만 지정하고 나머지는 기본값으로 채웁니다. (3.1에서 정적 팩토리로 통일한 것과 같은 이유 — 생성 로직을 한 곳에 모아둔다.)

### 4.4 실패 케이스는 `assertThrows(BusinessException.class)` + `ErrorCode` 검증으로 표준화한다

`~OrThrow`/`BusinessException` 패턴(1.6, 2.3)을 쓰기로 했으므로, 예외 테스트도 예외 타입과 `ErrorCode`만 검증합니다. 메시지 문자열은 assert하지 않습니다 — 2.3에서 메시지는 다국어 대응 시 바뀔 수 있는 값으로 남겨뒀기 때문입니다.

### 4.5 다중 구현체 인터페이스는 계약 테스트(Contract Test)를 공유한다

3.7에서 여러 구현체 + Failover 구조를 적용하기로 결정한 인터페이스에 한해, 인터페이스 레벨의 공통 테스트 스윗을 두고 모든 구현체가 그것을 통과하게 합니다. 새 구현체를 추가하거나 기존 구현체를 교체해도 Failover 로직이 깨지지 않는다는 것을 보장합니다.

### 4.6 테스트는 개발자의 로컬 데이터베이스에 의존하지 않는다

`./gradlew test`는 별도의 수동 준비 없이 반복 실행할 수 있어야 합니다. 데이터베이스가 필요한 Repository/통합 테스트는 Testcontainers가 격리된 PostgreSQL을 제공하며, `localhost`의 개발용 데이터베이스나 개인 `.env` 값에 의존하지 않습니다. Docker 실행 환경은 필요한 전제 조건으로 문서화합니다.

### 4.7 TDD는 엔티티와 Service 핵심 로직에 권장하고, 나머지는 구현과 함께/이후 테스트해도 된다

TDD(테스트를 먼저 작성하고 Red-Green-Refactor로 진행하는 방식)는 계층마다 비용과 효과가 달라서, 전 계층에 똑같이 강제하지 않습니다.

- **권장 (TDD로 진행)**: 엔티티의 불변식·상태 전이(7.4, 7.7), Service의 핵심 비즈니스 로직. 컨테이너 없이 즉시 실행되는 계층이라(4.1) 사이클이 실제로 빠르게 돕니다.
- **허용 (구현과 함께, 또는 구현 후 작성)**: Repository, Controller, 통합 테스트. `@DataJpaTest`/`@WebMvcTest`/Testcontainers 셋업 비용 때문에 사이클이 느려져 "테스트 먼저"의 효과가 줄어듭니다. 이 계층은 테스트가 **머지 전에 존재하고 통과하면** 됩니다(5.1).
- 이 절은 `반드시`/`금지`가 아닌 기본 권장입니다 — "테스트를 정말 먼저 썼는지"는 커밋 순서 외에는 리뷰로 확인하기 어렵고, 소규모 팀에서 강제해도 검증 비용만 커집니다. 실제로 강제되는 것은 5.1(테스트 통과)과 4.1~4.6(테스트가 있다면 어떤 모양이어야 하는가)뿐입니다.
- Repository/통합 테스트처럼 느린 테스트를 반복 실행해야 할 때는 `./gradlew test --tests "*CardRepositoryTest"`처럼 대상 클래스만 지정해 사이클을 짧게 유지합니다.

---

## 5. 규칙의 자동 검증

### 5.1 테스트 통과를 변경의 기본 조건으로 둔다

모든 PR과 기본 브랜치에서 `./gradlew test`가 통과해야 합니다. 새 코드 때문에 테스트 실행에 외부 서비스가 필요해지면 해당 서비스를 컨테이너나 테스트 대역으로 제공해 재현 가능한 상태를 유지합니다.

### 5.2 기계적으로 판별 가능한 규칙은 도구로 검증한다

포매터(Spotless)와 ArchUnit을 도입했습니다 (`build.gradle`).

- Spotless(`java` 블록) — import 정렬, 미사용 import 제거, 줄 끝 공백 제거를 강제합니다. 기존 코드가 탭 들여쓰기를 쓰고 있어 google-java-format처럼 전체를 재포맷하는 포매터는 쓰지 않고(스페이스로 강제 변환되어 기존 스타일과 충돌·대량 diff가 생김), `indentWithTabs()`로 현재 스타일만 기계적으로 강제합니다. `./gradlew spotlessCheck`는 `check` 태스크에 묶여 있어 `./gradlew check`/CI에서 자동으로 실행되고, 위반 시 `./gradlew spotlessApply`로 고칩니다.
- ArchUnit(`src/test/java/.../ArchitectureTest.java`) — Controller의 Repository 직접 참조 금지(3.2)를 검증합니다. 아직 `controller`/`repository` 패키지가 없는 스켈레톤 단계라 `allowEmptyShould(true)`로 공집합을 허용해 두었고, 첫 Controller/Repository가 추가되면 이 옵션을 제거해 실제로 검증되게 합니다. 계층/패키지 의존 규칙이 늘어나면 이 클래스에 테스트 메서드를 추가합니다. DDD 의존 규칙(7.6: 엔티티의 상위 계층 의존 금지, 다른 도메인의 repository/entity/service.impl 직접 참조 금지, 엔티티의 public 세터 금지, `protected` 기본 생성자)도 같은 클래스에서 검증합니다. 다른 도메인 엔티티를 가리키는 `@ManyToOne` 등은 필드 타입 의존이므로 도메인 간 참조 규칙에 함께 걸립니다. 같은 도메인 안에서 Aggregate Root가 다른 Aggregate를 연관관계로 참조하는 경우는 코드로 구분할 수 없어 리뷰로 확인합니다(5.3).

### 5.3 자동화하기 어려운 항목은 PR 체크리스트로 확인한다

PR에서는 최소한 다음 항목을 확인합니다.

- 엔티티 변경에 Flyway 마이그레이션과 Repository 테스트가 포함되었는가?
- 접근 제어와 상태 전이의 실패 케이스가 테스트되었는가?
- 이벤트·스케줄러·외부 호출이 재시도, 중복 실행, 유실 가능성을 다루는가?
- 엔티티의 불변식과 상태 전이가 서비스가 아니라 엔티티 안에 있는가? (7.4)
- 같은 도메인이라도 다른 Aggregate는 연관관계가 아니라 ID로 참조하는가? (7.3)
- 이 문서의 기본값을 벗어났다면 이유와 대안이 기록되었는가?

### 5.4 AI가 작성하거나 보조한 코드는 추가로 다음을 확인한다

Claude Code 등 AI 도구가 작성/수정한 코드는 사람이 작성한 코드보다 "요청 범위를 넘어선 변경"과 "불필요한 방어적 코드"가 섞여 들어가기 쉬우므로, 머지 전에 다음을 리뷰어가 직접 확인합니다.

- 요청한 범위를 벗어난 리팩토링, 포매팅 변경, 관련 없는 파일 수정이 섞여 있지 않은가?
- 실제로 발생하지 않는 입력/예외에 대한 방어 코드(불필요한 null 체크, try-catch, fallback, 아직 쓰이지 않는 설정값)가 "혹시 몰라서" 추가되지 않았는가?
- 지금 요구사항에 없는 미래 확장을 가정한 인터페이스·추상화가 들어가지 않았는가 — 3줄짜리 분기를 위해 인터페이스를 새로 만드는 식의 과설계는 없는가?
- 주석이 "왜"가 아니라 "무엇을"을 설명하고 있지 않은가? 코드가 이름만으로 충분히 설명되면 주석은 불필요합니다. (세부 기준은 6.3에서 정하는 중, TBD)
- 네이밍·패키지 배치가 1~4장의 기존 컨벤션과 다르지 않은가?
- 테스트가 실제 동작(given 상태 → when 행동 → then 결과)을 검증하는지, 구현을 그대로 베껴 항상 통과하도록 만든 테스트는 아닌지?

---

## 6. 클린 코드 세부 기준

### 6.1 메서드와 클래스는 책임 하나로 작게 유지한다

숫자는 강제 규칙이 아니라 "이 메서드/클래스가 여러 일을 하고 있는가?"를 리뷰에서 판단하는 참고선입니다.

- public 메서드 본문은 스크롤 없이 한 화면에서 읽을 수 있는 길이(대략 15~20줄)를 넘지 않는 것을 기본으로 하고, 넘으면 의미 있는 단위로 `private` 메서드 추출을 고려합니다.
- 분기(`if`/`else`, `switch`)가 3단 이상 중첩되면 조기 반환(guard clause)이나 메서드 추출로 중첩을 줄입니다.
- 하나의 서비스/컨트롤러 클래스가 서로 관련 없는 여러 책임(예: 조회 + 알림 발송 + 외부 API 호출)을 동시에 갖게 되면 클래스 분리를 고려합니다 — 1.1의 Read/Write 분리, 3.4의 Scheduler 분리와 같은 원칙의 연장입니다.
- TBD: 이 기준을 기계적으로 검증하려면 Checkstyle의 `MethodLength`/`CyclomaticComplexity` 모듈 도입을 검토할 수 있습니다. 지금은 Spotless+ArchUnit 범위를 넘으므로 리뷰로만 확인합니다.

### 6.2 PR과 커밋은 도메인/관심사 단위로 쪼갠다

- 하나의 PR은 가능한 한 하나의 도메인 변경 또는 하나의 관심사만 다룹니다. 예: 엔티티+Flyway 마이그레이션+Repository는 한 PR, 그 위의 Service/Controller는 이어지는 별도 PR로 쪼갤 수 있습니다 — 단, 엔티티 변경과 대응 마이그레이션은 반드시 같은 PR(커밋)에 포함합니다 (3.8).
- 커밋은 "컴파일되고 테스트가 통과하는 단위"로 나눕니다. 포매팅/리네임처럼 기계적인 변경과 로직 변경을 같은 커밋에 섞지 않아야 리뷰어가 커밋 단위로 따라 읽을 수 있습니다.
- AI에게 여러 도메인에 걸친 변경을 한 번에 요청했더라도, 실제 커밋/PR은 위 기준대로 쪼개도록 (AI에게든, 직접 재구성해서든) 명시적으로 처리합니다.

주석 작성 기준과 로그 메시지 언어 통일 규칙은 별도로 논의 중이라 아직 이 문서에 추가하지 않았습니다 (6.3으로 추가 예정, TBD).

---

## 7. 도메인 주도 설계(DDD) 규칙

도메인별 패키지(`CLAUDE.md` "최상위 패키지 레이아웃")를 바운디드 컨텍스트의 출발점으로 삼고, DDD의 전술 패턴을 실용적인 수준(DDD-lite)으로 적용합니다. 별도의 `domain`/`application`/`infrastructure` 레이어나 도메인 모델과 JPA 엔티티의 분리는 도입하지 않습니다 — 현재 규모에서 매핑 코드 비용이 이득보다 큽니다. 도메인 모델은 JPA 엔티티가 겸하며, 이 선택은 7.8의 조건에서 재검토합니다.

### 7.1 도메인 패키지는 하나의 바운디드 컨텍스트로 취급한다

`card`, `user` 같은 최상위 도메인 패키지는 자기 언어(용어)와 규칙을 가집니다. 같은 단어(예: `user`)가 도메인마다 다른 의미라면 억지로 하나의 클래스로 합치지 않고 각 도메인에서 필요한 모양으로 따로 표현합니다. 용어는 코드, 테스트 이름(`@DisplayName`), API 문서에서 동일하게 씁니다.

### 7.2 Aggregate 단위로 엔티티를 설계하고, 리포지토리는 Aggregate Root당 하나만 둔다

- Aggregate 안의 하위 엔티티는 Root를 통해서만 생성·변경하며, 하위 엔티티 전용 `Repository`를 만들지 않습니다.
- 하나의 트랜잭션에서는 하나의 Aggregate만 변경하는 것을 기본으로 합니다. 여러 Aggregate를 함께 바꿔야 하면 7.5의 도메인 이벤트로 분리할 수 있는지 먼저 검토합니다.
- Aggregate는 작게 유지합니다. 함께 변경되어야 하는 불변식(invariant)이 없다면 같은 Aggregate로 묶지 않습니다.

### 7.3 Aggregate 사이(특히 도메인 사이)는 객체가 아니라 ID로 참조한다

다른 Aggregate는 `@ManyToOne` 등 연관관계 대신 ID(`UUID` 등)만 필드로 가집니다. 다른 도메인 패키지의 엔티티를 import해 필드로 들고 있는 것을 금지합니다. 조회 시 함께 필요한 값은 서비스에서 각 리포지토리로 조회해 조합하거나, 읽기 전용 프로젝션(3.3)을 사용합니다. 같은 Aggregate 내부의 엔티티/값 객체 연관은 허용합니다.

### 7.4 비즈니스 규칙과 상태 전이는 엔티티(도메인 객체) 안에 둔다

서비스는 트랜잭션 경계, 조회, 권한 확인, 도메인 객체 호출 순서를 조율하고, 불변식과 상태 전이 검증은 엔티티 메서드가 담당합니다(빈약한 도메인 모델 지양).

- 엔티티는 세터를 노출하지 않고, 생성은 정적 팩토리(3.1)로 하며 생성 시점에 불변식을 검증합니다. 기본 생성자는 `protected`로 둡니다.
- 상태 변경 메서드는 1.5의 접두사 규칙을 기본으로 하되, 단순 필드 교체가 아니라 비즈니스 의도를 가진 동작이면 의도를 드러내는 동사(`cancel()`, `complete()`, `expire()`)를 씁니다.
- 규칙 위반은 엔티티 안에서 `BusinessException`으로 던집니다(2.3). 서비스의 `if (status == ...)` 검증을 엔티티로 옮기는 것이 기본입니다. (2.1은 엔티티 하나에 속하지 않는 접근 제어·소유자 검증에 대한 규칙입니다.)
- 하나의 Aggregate 안에 속하지 않는 여러 Aggregate에 걸친 규칙은 `~Policy` 또는 `~DomainService`로 두며, 스프링 빈이지만 리포지토리·외부 호출 없이 도메인 객체만 인자로 받는 순수 로직으로 유지합니다.

### 7.5 값 객체(Value Object)와 도메인 이벤트를 활용한다

- 의미 있는 값 묶음(좌표, 금액, 기간 등)과 ID 타입은 `record` 또는 `@Embeddable` 불변 값 객체로 표현합니다. 원시 타입 여러 개를 파라미터로 나열하지 않습니다. 값 객체는 생성 시 스스로 유효성을 검증하고 동등성은 값으로 비교합니다.
- 한 Aggregate의 변경이 다른 Aggregate나 부수효과(알림 등)를 일으켜야 하면 도메인 이벤트(`~Event`, 과거형 명명: `CardCancelledEvent`)를 발행하고, 커밋 이후 처리는 3.5(`@TransactionalEventListener(AFTER_COMMIT)`)를 따릅니다. 이벤트에는 엔티티가 아닌 원시값/ID만 담습니다(2.4). 이벤트 클래스는 발행하는 도메인의 `event` 패키지에 둡니다.
- 이벤트 리스너가 다른 도메인에 속하면, 이벤트를 발행하는 쪽이 리스너를 알지 못해야 합니다(발행 도메인 → 구독 도메인 의존 금지).

### 7.6 의존 방향 규칙

- `entity`(도메인 모델)는 `controller`, `service`, `dto`, `repository`에 의존하지 않습니다. 의존 방향은 항상 `controller → service → repository → entity`와 `service → entity`입니다.
- 도메인 사이의 호출은 상대 도메인의 서비스 인터페이스(`service` 패키지의 Read/Write 인터페이스) 또는 이벤트를 통합니다. 상대 도메인의 `repository`, `entity`, `service.impl`을 직접 참조하지 않습니다.
- 도메인 패키지 사이에 순환 의존이 생기면 7.5의 이벤트 또는 2.2의 방법으로 끊습니다.
- 위 규칙은 ArchUnit(5.2)으로 검증합니다.

### 7.7 테스트 방식

- Aggregate의 불변식과 상태 전이는 엔티티 단위 테스트(컨테이너·Mockito 불필요)로 먼저 검증합니다 — 4.7에서 TDD를 권장하는 범위와 같습니다. 서비스 테스트는 조율(호출 순서, 이벤트 발행, 권한)에 집중합니다.
- 값 객체는 유효/무효 입력 경계를 단위 테스트로 검증합니다.
- 4.4의 `assertThrows(BusinessException.class)` + `ErrorCode` 검증 방식을 엔티티 테스트에도 그대로 적용합니다.

### 7.8 이 선택을 재검토하는 조건 (TBD)

다음 중 하나가 발생하면 도메인 모델과 JPA 엔티티의 분리, 레이어 재구성을 논의하고 이 문서와 `CLAUDE.md`를 함께 갱신합니다.

- 엔티티가 영속성 애너테이션 때문에 도메인 규칙을 표현하기 어려워지는 경우
- 같은 도메인을 여러 저장소/외부 시스템으로 읽고 쓰는 경우
- 한 도메인이 별도 서비스로 분리되어야 하는 경우

---

## 요약

| 구분 | 문서화한 규칙 수 |
|---|---|
| 1. 네이밍 규칙 | 7 |
| 2. 정책/검증 코드 배치 기준 | 4 |
| 3. 기타 구조적 관례 | 8 |
| 4. 테스트 규칙 | 7 |
| 5. 규칙의 자동 검증 | 4 |
| 6. 클린 코드 세부 기준 | 2 (+TBD 1) |
| 7. 도메인 주도 설계(DDD) 규칙 | 7 (+TBD 1) |
| **합계** | **39** |
