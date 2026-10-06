# 작업: {도메인} — {한 줄 요약}

> 관리자가 작성하는 작업 스펙입니다. 검증자는 이 문서로 테스트를 쓰고, 작성자는 그 테스트를 통과시킵니다. 원천 스펙(DBML, Notion)의 해당 부분을 이 문서에 옮겨 적어 이 문서만으로 작업할 수 있게 합니다.

## 범위
- 포함: (예: 엔티티, 값 객체, Flyway 마이그레이션, Repository)
- 제외: (예: Service, Controller)
- 패키지: `com.cusfit.cusfitbe.{domain}`
- 마이그레이션 파일: `V{n}__{설명}.sql`

## Aggregate
| Aggregate Root | 내부 구성(자식 엔티티 / 값 객체) | 다른 Aggregate 참조(ID) |
|---|---|---|

## 불변식과 상태 전이
- 생성: (정적 팩토리 이름, 검증 규칙, 위반 시 ErrorCode)
- 상태 전이: (메서드, 허용되는 이전 상태 → 다음 상태, 위반 시 ErrorCode)

## 테이블 (원천 스펙 발췌 + PostgreSQL 변환)
```sql
-- 컬럼, 타입, NOT NULL, 기본값, 인덱스, 제약 조건
```

## Repository에서 필요한 조회/변경
- (메서드 의도. 이름은 컨벤션 1.6/1.7을 따른다)

## 완료 기준
- [ ] 검증자의 테스트가 모두 통과한다
- [ ] `./gradlew check` 통과 (Spotless, ArchUnit, Testcontainers, contextLoads의 ddl validate)
- [ ] 검증자 review VERDICT: PASS

## 미결정 / 가정
- (스펙이 정하지 않아 가정한 것. 사용자 확인이 필요하면 표시)
