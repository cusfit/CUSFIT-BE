# 작업: notification-basic — notification 기본 구조

## 배정
- 작업 ID: notification-basic
- 선행 작업: report-basic
- 작성자 허용 경로: src/main/java/com/cusfit/cusfitbe/notification, src/main/resources/db/migration/V6__create_notifications.sql
- 검증 대상: '*Notification*Test' (실제 테스트명은 검증자 보고서)
- public API / Aggregate / 테이블 / 실패 계약: docs/tasks/domain-basics-contract.md의 해당 도메인과 공통 계약을 모두 따른다.

## 변경
Notification 저장·조회. UserReadService 참조 검증. 발송 없음.
- Flyway: src/main/resources/db/migration/V6__create_notifications.sql

## 완료 기준
- 검증자가 src/test/**에 테스트 작성 후 관리자가 구현 부재 Red를 확인한다.
- 작성자는 src/main/** 허용 경로만 수정하며 테스트/문서/설정 스크립트/커밋은 금지한다.
- ./gradlew check 통과. 독립 리뷰 PASS와 pipeline 통합까지 완료.
- DB 테스트는 Testcontainers postgis/postgis:16-3.4; 로컬 DB나 외부 인증에 의존하지 않는다.

