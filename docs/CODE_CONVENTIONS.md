# 코드 컨벤션

`src/main/java/com/deoham/**`의 기존 코드를 근거로 정리한 컨벤션 문서입니다. 패키지 레이아웃, `ApiResponse` 응답 포맷, 인증 흐름, 프로필/스키마 관리 등 아키텍처 규칙은 이미 [CLAUDE.md](../CLAUDE.md)에 정리되어 있으므로 여기서는 다루지 않습니다 (해당 항목은 CLAUDE.md 참조).

---

## 1. 네이밍 규칙

### 1.1 Service 인터페이스는 Read/Write로 분리한다

읽기 전용 조회와 상태 변경 로직을 별도 인터페이스로 분리하는 것이 기본 패턴입니다.

- 근거: `card/service/CardReadService.java:11`, `card/service/CardWriteService.java` — `CardReadService`(조회), `CardWriteService`(생성/취소/재시도 등)로 분리.
- 근거: `user/service/UserReadService.java`, `user/service/UserWriteService.java` — 동일 패턴이 `user` 패키지에도 적용됨.

### 1.2 구현체 네이밍은 패키지 간 불일치가 존재한다 (주의)

같은 Read/Write 분리 패턴이라도 구현 클래스 네이밍/위치가 도메인마다 다릅니다. 신규 코드 작성 시 어느 쪽을 따를지 사전에 팀 합의가 필요합니다.

- 근거: `card/service/DefaultCardReadService.java`, `card/service/DefaultCardWriteService.java:33` — 인터페이스와 **같은 패키지**에 `Default` 접두사로 위치 (`public class DefaultCardWriteService implements CardWriteService`).
- 근거: `user/service/impl/UserReadServiceImpl.java:18` — `impl` **하위 패키지**에 `Impl` 접미사로 위치 (`public class UserReadServiceImpl implements UserReadService`).

### 1.3 DTO는 역할에 따라 `Request`/`Response` 접미사를 붙인다

요청 바디는 `~Request`, 응답 바디는 `~Response`로 끝나며, `card` 패키지는 `dto/request`, `dto/response` 하위 패키지로 추가 분리되어 있습니다.

- 근거: `card/dto/request/CreateCardRequest.java`, `card/dto/response/CardDetailResponse.java` — 요청/응답 DTO가 별도 하위 패키지.
- 근거: `chat/dto/ChatMessageSendRequest.java`, `chat/dto/ChatMessageResponse.java` — `chat`은 하위 패키지 분리 없이 같은 `dto` 패키지에 이름으로만 구분.

### 1.4 Controller는 `~Controller`, Swagger 문서는 `controller/docs`의 `~ControllerDocs` 인터페이스로 분리한다

`@Operation`, `@ApiResponse` 등 Swagger 애너테이션은 컨트롤러 구현체가 아니라 별도 인터페이스에 선언하고, 컨트롤러가 이를 `implements`합니다.

- 근거: `card/controller/docs/CardControllerDocs.java:20` — `public interface CardControllerDocs { @Operation(...) ResponseEntity<CardDetailResponse> createCard(...); }`
- 근거: `card/controller/CardController.java:32` — `public class CardController implements CardControllerDocs` — 실제 구현에는 `@Override`만 있고 Swagger 애너테이션은 없음.
- 동일 패턴: `chat/controller/docs/ChatRoomControllerDocs.java`, `notification/controller/docs/NotificationControllerDocs.java`, `report/controller/docs/ReportControllerDocs.java`, `user/controller/docs/UserControllerDocs.java`.

### 1.5 엔티티의 상태 변경 메서드는 동사 접두사로 의도를 드러낸다

세터(setter) 대신 `update~`(필드 값 교체), `increment~`(카운터 증가), `mark~`(플래그/이벤트성 상태 전환) 패턴을 사용합니다.

- 근거: `card/entity/Card.java:95-103` — `updateStatus(CardStatus status)`, `incrementRetryCount()`, `updateExpiresAt(Instant expiresAt)`.
- 근거: `user/entity/User.java:101-148` — `incrementHelpCount()`, `incrementHelpRequestCount()`, `updateProfile(...)`, `markCardCreated()`, `markCardViewOnboardingSeen()`.

### 1.6 조회 실패를 던지는 리포지토리 헬퍼는 `~OrThrow` 접미사를 쓴다

리포지토리를 감싸 "찾거나 예외" 로직을 제공하는 메서드는 `find~OrThrow` 형태로 명명합니다 (1.7 항목의 Guard/AccessService 클래스에서 반복).

- 근거: `chat/service/ChatAccessGuard.java:24,29` — `findMessageOrThrow(UUID messageId)`, `findRoomOrThrow(UUID roomId)`.
- 근거: `chat/service/ChatRoomAccessService.java:31` — `findRoomOrThrow(UUID roomId)` (동일 시그니처가 별도 클래스에도 존재 — 2.2 항목 참고).

### 1.7 Spring Data 리포지토리 메서드는 쿼리 메서드 규약을 그대로 사용한다

커스텀 `@Query` 없이도 표현 가능한 조회는 `find/exists/delete + By + 조건` 형태의 메서드명을 사용합니다.

- 근거: `card/repository/CardRepository.java:16-20` — `findByRequesterAndStatusIn(...)`, `findFirstByRequesterIdAndStatusIn(...)`, `findByStatusInAndExpiresAtBefore(...)`.

---

## 2. 정책/검증 코드의 배치 기준

### 2.1 단일 서비스 내부에서만 쓰이는 검증은 `private` 메서드로 둔다

특정 서비스의 여러 public 메서드에서 반복되지만 다른 도메인/클래스에서 재사용되지 않는 검증은 같은 서비스 클래스의 `private` 메서드로 추출합니다.

- 근거: `card/service/DefaultCardWriteService.java:202-209` — `private Card findCardAndValidateOwner(UUID cardId, UUID userId)`가 `cancelCard`, `completeCard`, `retryCard` 3곳에서 재사용되지만 `DefaultCardWriteService` 내부에만 존재.
- 대비: 같은 파일의 상태 전이 검증(`if (card.getStatus() != CardStatus.OPEN) throw ...`, `DefaultCardWriteService.java:94-96,119-121,136-138`)은 메서드마다 조건이 달라 별도 추출 없이 각 public 메서드 내부에 인라인으로 남아 있음 — 반복되는 "판별 로직"만 메서드로 뽑고, 메서드별로 다른 1회성 조건은 인라인 유지.

### 2.2 여러 서비스/컨트롤러 경계를 넘어 재사용되는 접근 제어는 별도 `@Component`(Guard/AccessService)로 분리한다

같은 도메인이라도 순환 의존을 끊어야 하거나 여러 진입점(REST 컨트롤러 + STOMP 인터셉터 등)에서 동일 검증이 필요하면, 검증 로직을 서비스가 아닌 별도 컴포넌트로 승격합니다. 이 저장소에는 이 원칙을 지키려다 두 클래스에 로직이 중복된 사례가 실제로 존재합니다.

- 근거: `chat/service/ChatRoomAccessService.java:18-21` (클래스 주석) — "ChatMessageService(및 그 하위 SimpMessagingTemplate 의존성)를 거치지 않도록 분리한 채팅방 조회/참여자 검증 컴포넌트. StompAuthChannelInterceptor가 ChatRoomService를 직접 의존하면 WebSocket 메시지 브로커 빈 그래프와 순환 의존이 생기므로 여기서 끊는다."
- 근거: `chat/service/ChatAccessGuard.java:34-42`와 `chat/service/ChatRoomAccessService.java:39-48`에 `requireParticipant(Card card, UUID userId)`가 동일 로직으로 각각 존재 — 순환 의존 회피를 위해 의도적으로 분리·중복시킨 사례. 신규 접근 제어 로직을 추가할 때는 "이 검증이 다른 진입점(STOMP 등)에서도 필요한가"를 먼저 판단 기준으로 삼아야 함.

### 2.3 도메인 예외는 공통 `BusinessException` + `ErrorCode` enum으로 표현하고, 메시지는 호출부에서 문자열로 전달한다

`ErrorCode`는 HTTP 상태별로 소수의 범용 코드만 정의하고, 구체적인 실패 사유는 `BusinessException` 생성 시 두 번째 인자(message)로 넘깁니다. 즉 "정책 위반 종류"를 늘릴 때 `ErrorCode`에 항목을 추가하기보다 기존 코드 + 메시지 조합을 우선 사용하는 것이 현재 관례입니다.

- 근거: `global/exception/ErrorCode.java:11-16` — `INVALID_REQUEST`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_ERROR` 6종만 존재.
- 근거: `card/service/DefaultCardWriteService.java:95` — `throw new BusinessException(ErrorCode.CONFLICT, "OPEN 상태의 카드만 취소할 수 있습니다.");` — 동일 `ErrorCode.CONFLICT`를 서로 다른 메시지로 재사용.
- 근거: `global/exception/BusinessException.java:15-18` — `public BusinessException(ErrorCode errorCode, String message)` 생성자가 이 패턴을 위해 명시적으로 제공됨.

### 2.4 트랜잭션 스코프 안에서만 유효한 값은 별도 record로 "탈출"시켜 비트랜잭션 계층에 넘긴다

`open-in-view: false` 제약(CLAUDE.md 참조) 때문에, 트랜잭션 밖에서 지연 로딩이 필요한 값은 트랜잭션 메서드 안에서 순수 값 객체로 변환해 반환하는 방식을 정책적으로 사용합니다.

- 근거: `chat/service/ChatRoomAccessService.java:60-69` — `cardTargetLocation(UUID roomId)` 주석: "LiveLocationService는 비트랜잭션(+open-in-view=false)이므로 lazy 엔티티 탐색을 이 트랜잭션 경계 안에서 끝내고 순수 좌표만 넘긴다." → 내부 `record CardTargetLocation(double latitude, double longitude)`로 변환 후 반환.
- 근거: `notification/event/FcmPushEvent.java` 클래스 주석 — "엔티티가 아니라 원시값만 담아 트랜잭션 커밋 이후(다른 스레드)에도 detached/lazy 로딩 문제가 없도록 한다."

---

## 3. 기타 반복되는 구조적 관례

### 3.1 DTO → Entity 변환은 DTO의 정적 팩토리 메서드(`from`/`of`)로 하거나, 서비스에서 생성자를 직접 호출하는 두 방식이 혼재한다

일부 도메인은 `XxxResponse.from(entity)` 정적 팩토리를 두지만, 다른 도메인(특히 `card`)은 서비스 메서드 안에서 레코드 생성자를 직접 호출합니다. 새 DTO를 만들 때는 같은 패키지 내 기존 관례(정적 팩토리 유무)를 우선 따르는 것이 안전합니다.

- 정적 팩토리 사용: `user/dto/ProfileResponse.java:25` — `public static ProfileResponse from(User user) { ... }`
- 정적 팩토리 사용: `report/dto/ReportResponse.java:42` — `public static ReportResponse from(Report report)`, `notification/dto/NotificationResponse.java:17` — `public static NotificationResponse from(Notification notification)`, `card/dto/response/MyActiveCardResponse.java:15` — `public static MyActiveCardResponse of(CardDetailResponse card, boolean hasCreatedCard)`.
- 서비스에서 직접 생성: `card/service/DefaultCardWriteService.java:70-86` — `createCard`가 `new CardDetailResponse(card.getId(), card.getRequester().getId(), ...)`로 14개 필드를 직접 나열.

### 3.2 컨트롤러는 리포지토리를 직접 참조하지 않고 항상 서비스 계층을 통한다

`src/main/java/com/deoham/*/controller/*.java` 전체에서 `*.repository.*` import가 발견되지 않습니다. 컨트롤러 → 서비스 → 리포지토리 순서가 예외 없이 지켜집니다.

- 근거: `grep -rl "import com.deoham.*repository" src/main/java/com/deoham/*/controller/*.java` → 매칭 없음 (컨트롤러 전체 탐색 결과).
- 근거: `card/controller/CardController.java:34-35` — 필드가 `CardReadService`, `CardWriteService`만 존재, 리포지토리 필드 없음.

### 3.3 목록/집계성 조회는 Spring Data 인터페이스 프로젝션을 사용한다

단순 엔티티 반환이 아니라 일부 컬럼만 필요한 집계 쿼리는 `~Projection` 인터페이스로 결과를 매핑합니다.

- 근거: `chat/repository/UnreadCountProjection.java` — `interface UnreadCountProjection { UUID getRoomId(); Long getUnreadCount(); }`
- 근거: `chat/repository/LastMessageProjection.java` — 채팅방 목록의 마지막 메시지 요약을 위한 동일 패턴.

### 3.4 주기 실행 로직은 `~Scheduler` 컴포넌트로 분리하고, 실제 상태 변경은 반드시 Write 서비스에 위임한다

`@Scheduled` 메서드 자신은 엔티티나 리포지토리를 직접 다루지 않고, 트랜잭션 처리는 서비스로 위임한 뒤 예외를 로깅만 하고 삼킵니다(스케줄러 스레드가 죽지 않도록).

- 근거: `card/scheduler/CardScheduler.java` — `@Scheduled(fixedDelay = 60000) public void expireCards() { try { cardWriteService.expireCards(); } catch (Exception e) { log.error(...); } }` — 리포지토리 의존 없이 `CardWriteService` 한 개만 주입받음.
- 동일 패턴: `user/scheduler/UserScheduler.java`.

### 3.5 부수 효과(알림 발송 등)는 도메인 이벤트 발행(`ApplicationEventPublisher`)으로 분리한다

알림처럼 트랜잭션 커밋 이후에 일어나야 하거나 다른 스레드/비동기로 처리되어야 하는 부수 효과는 서비스가 직접 호출하지 않고 이벤트를 발행한 뒤 별도 리스너가 처리합니다.

- 근거: `notification/service/NotificationService.java:41,64,86` — 여러 알림 시점에서 `eventPublisher.publishEvent(new FcmPushEvent(...))` 호출.
- 근거: `notification/service/FcmSender.java:39` — `@Async("fcmTaskExecutor")`로 실제 발송은 별도 스레드에서 비동기 처리.
- 근거: `chat/event/LiveLocationSessionListener.java:42,57,64` — `@EventListener`로 세션 관련 이벤트 수신.

### 3.6 외부 연동 설정은 `~Config` + `~Properties` 쌍으로 분리한다

외부 시스템(S3, DeepL, Gemini 등) 연동은 `@ConfigurationProperties`를 붙인 불변 `record ~Properties`와, `@EnableConfigurationProperties`로 이를 활성화하며 `@Bean`을 만드는 `~Config` 클래스로 항상 쌍을 이룹니다.

- 근거: `global/config/S3Properties.java:9-10` — `@ConfigurationProperties(prefix = "deoham.s3") public record S3Properties(...)` / `global/config/S3Config.java:13,15` — `@EnableConfigurationProperties(S3Properties.class) public class S3Config { @Bean ... }`.
- 동일 패턴: `DeepLConfig`+`DeepLProperties` (`prefix = "deoham.deepl"`), `GeminiConfig`+`GeminiProperties` (`prefix = "deoham.gemini"`).

### 3.7 필터/번역기 등 교체 가능한 구현은 인터페이스 + 여러 구현체 + Failover/Circuit-breaker 래퍼로 구성한다

외부 API 실패에 대비해 단일 구현에 의존하지 않고, 인터페이스를 둔 뒤 우선순위가 다른 구현체를 두고, 이를 감싸는 Failover 구현체가 서킷 브레이커로 전환을 판단합니다.

- 근거: `chat/translation/TranslationProvider.java` (인터페이스) — 구현체 `chat/translation/DeepLTranslationProvider.java`, `chat/translation/GeminiTranslationProvider.java`, `chat/translation/DummyTranslationProvider.java`.
- 근거: `chat/translation/FailoverTranslationProvider.java`, `chat/translation/SimpleCircuitBreaker.java` — 개별 Provider 장애 시 다음 Provider로 전환하는 래퍼.

---

## 요약

| 구분 | 문서화한 규칙 수 |
|---|---|
| 1. 네이밍 규칙 | 7 |
| 2. 정책/검증 코드 배치 기준 | 4 |
| 3. 기타 구조적 관례 | 7 |
| **합계** | **18** |
