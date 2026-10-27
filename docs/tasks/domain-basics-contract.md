# 개발용 도메인 기본 구조 — 확정 계약

사용자와 합의한 최소 저장·단건 조회 범위다. BUSINESS_POLICY/TECH_DECISIONS의 나머지 추천안은 확정하지 않는다.

## 공통 API
- 패키지 com.cusfit.cusfitbe.{domain}. controller/docs, controller, service, service/impl, repository, entity, dto/request, dto/response.
- 각 독립 모델 X: entity.X, repository.XRepository extends JpaRepository<X, Long>, service.XReadService / XWriteService 및 impl.XReadServiceImpl / XWriteServiceImpl.
- public XReadService.getById(Long id): XResponse. public XWriteService.create(CreateXRequest request): XResponse.
- DTO는 record. CreateXRequest의 필드는 아래 모델 표 순서. XResponse는 Long id, 모델 필드, Instant createdAt 순서. 정적 XResponse.from(X).
- 모든 독립 엔티티는 BaseCreatedEntity 상속. protected 기본 생성자, static X.create(모델 필드 순서), getter, setter 없음.
- BaseCreatedEntity: global.entity, @MappedSuperclass, Long id IDENTITY, Instant createdAt; @PrePersist로 생성 시각 설정. 공통 Getter만 노출. 이 단계에서는 수정이 없으므로 updatedAt 없음.
- 문자열은 null/blank 금지, 필수 ID와 Instant는 null 금지. 엔티티 팩토리도 이를 검증하여 BusinessException(INVALID_REQUEST) 발생. global.validation.DomainValidation의 public static void requireText(String value, String field), requirePresent(Object value, String field)를 사용.
- 요청 DTO: @NotBlank / @NotNull. 추가 길이, 닉네임 중복, URL 형식, 상태 및 정책 enum 검증 없음.
- Read 구현: @Transactional(readOnly=true), findById 또는 BusinessException(NOT_FOUND).
- Write 구현: @Transactional, 참조 ID를 각 ReadService.getById로 확인 후 엔티티 생성·저장·DTO 변환. 타 도메인 repository/entity/impl 직접 접근 금지.
- Controller: @Profile("local"), @RestController, /api/dev/{resource}. ControllerDocs implements. POST 생성 201 + Location(/api/dev/{resource}/{id}), GET /{id} 200; ApiResponse<XResponse>. MVC 매핑/Valid/PathVariable/RequestBody는 Controller에, Swagger @Operation은 docs 인터페이스에 둔다.
- 목록/수정/삭제 엔드포인트 없음. 인증 주체 대신 요청의 명시적 사용자 ID 사용. prod/test/default에서는 개발 Controller가 빈으로 등록되지 않는다.
- local SecurityFilterChain은 /api/dev/**만 permitAll 및 CSRF 제외, Swagger 문서 경로와 health 허용, 나머지는 denyAll. 운영 JWT/로그인 설계를 추가하거나 다른 프로필에 permitAll을 적용하지 않는다.
- malformed JSON/날짜/ID 타입은 기존 공통 예외 처리에 INVALID_REQUEST 400으로 매핑. 예외 응답은 기존 ApiResponse를 사용.

## 모델
| domain | X | resource/table | Create 필드(Java 타입) |
|---|---|---|---|
| user | User | users | String nickname |
| terms | Terms | terms | String type, String version, String contentUrl, Instant effectiveAt |
| catalog | Brand | brands | String name |
| catalog | Menu | menus | Long brandId, String name |
| catalog | OptionPreset | option-presets / option_presets | Long brandId, String name |
| recipe | Recipe | recipes | Long authorId, Long menuId, String title, List<CreateRecipeOptionRequest> options |
| report | Report | reports | Long reporterId, Long recipeId, String content |
| notification | Notification | notifications | Long recipientId, String title, String content |
| support | Notice | notices | String title, String content |
| support | Faq | faqs | String question, String answer |
| support | Inquiry | inquiries | Long authorId, String title, String content |

RecipeOption은 독립 Aggregate가 아니다. create 요청 항목은 String name만 가진다(@NotBlank). options 자체는 @NotNull @Valid이고 원소 null 금지, 빈 목록 허용. 팩토리 Recipe.create(Long authorId, Long menuId, String title, List<String> optionNames)는 옵션을 입력 순서(0부터)로 생성한다. 자식 RecipeOption도 BaseCreatedEntity를 상속하며 필드는 String name, int position이다. 자식 정적 생성 팩토리는 package-private로 두어 Recipe를 통해서만 생성한다. Recipe.getOptions()는 변경 불가능한 목록을 반환한다. 단방향 @OneToMany(cascade=ALL, orphanRemoval=true), @JoinColumn(name="recipe_id", nullable=false), @OrderBy("position ASC") 매핑을 사용한다. 응답의 options는 List<RecipeOptionResponse>(Long id, String name, int position, Instant createdAt), 트랜잭션 안에서 변환한다.

## SQL
- 각 테이블 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, created_at timestamptz NOT NULL.
- String 필드는 text NOT NULL(@Column(columnDefinition="text", nullable=false)), Instant 필드는 timestamptz NOT NULL, ID 참조는 bigint NOT NULL.
- FK: menus.brand_id / option_presets.brand_id -> brands.id, recipes.author_id -> users.id, recipes.menu_id -> menus.id, reports.reporter_id -> users.id, reports.recipe_id -> recipes.id, notifications.recipient_id -> users.id, inquiries.author_id -> users.id.
- recipe_options(recipe_id bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE, name text NOT NULL, position integer NOT NULL). 나머지 FK는 기본 NO ACTION. 도메인 간 cascade 없음.
- PK 외 unique/정책 check/검색 index 및 seed 추가 없음. 빈 문자열 검증은 엔티티와 요청에서 수행.
- 마이그레이션 순서: V1 users, V2 terms, V3 catalog, V4 recipes, V5 reports, V6 notifications, V7 support.

## 검증
각 도메인에 엔티티 팩토리의 유효/빈 문자열/누락 필드, 서비스 저장·조회·참조 실패, @DataJpaTest+Testcontainers 실제 FK 및 매핑, @WebMvcTest+MockMvc 생성·단건·400·404·201 Location을 검증한다. fixture 사용, 한글 DisplayName, 메시지 문자열에 의존하지 않는다. Recipe는 빈 옵션/순서/실제 재조회 후 자식 ID·createdAt을 검증한다. 공통 기반은 malformed 요청 400과 local 보안 한정, 기본 기반 엔티티 검증. 최종 local HTTP→실DB smoke 및 prod에서 개발 controller 미등록 검증을 포함한다. ./gradlew check 통과, 기존 ArchUnit 공집합 허용은 실제 해당 계층 도입 시 제거한다.

## 제외
소셜 로그인, 사용자 외부 인증 ID, 약관 동의, 수정·삭제·상태 전이, 탈퇴, 반응, AI, 신고 처리, 푸시 전송, S3, 파일 첨부, 이벤트/배치, 목록/검색, 운영 데이터. 상세 정책을 임의로 추가하지 않는다.

