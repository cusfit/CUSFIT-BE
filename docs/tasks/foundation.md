# 작업: foundation — 공통 기반 기본 구조

## 배정
- 작업 ID: foundation
- 선행 작업: 없음
- 작성자 허용 경로: src/main/java/com/cusfit/cusfitbe/global
- 검증 대상: '*Foundation*Test' (실제 테스트명은 검증자 보고서)
- public API / Aggregate / 테이블 / 실패 계약: docs/tasks/domain-basics-contract.md의 해당 도메인과 공통 계약을 모두 따른다.

## 변경
BaseCreatedEntity, DomainValidation, local SecurityConfig, malformed request 400 처리. 기존 global 응답 계약 유지. 엔티티 기반은 테스트 전용 엔티티/단위 검사로 확인. 공통 검증은 기존 contextLoads에도 영향이 없도록 설계.
- Flyway 없음. 공통 설정에만 집중.

## 완료 기준
- 검증자가 src/test/**에 테스트 작성 후 관리자가 구현 부재 Red를 확인한다.
- 작성자는 src/main/** 허용 경로만 수정하며 테스트/문서/설정 스크립트/커밋은 금지한다.
- ./gradlew check 통과. 독립 리뷰 PASS와 pipeline 통합까지 완료.
- DB 테스트는 Testcontainers postgis/postgis:16-3.4; 로컬 DB나 외부 인증에 의존하지 않는다.

