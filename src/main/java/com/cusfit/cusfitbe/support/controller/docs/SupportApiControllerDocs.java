package com.cusfit.cusfitbe.support.controller.docs;

import com.cusfit.cusfitbe.global.response.ApiResponse;
import com.cusfit.cusfitbe.support.dto.request.SubmitInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Support")
public interface SupportApiControllerDocs {

	@Operation(summary = "S1. 공지 목록", description = "등록일 최신순. 중요 공지 상단 고정은 notices에 중요 여부 컬럼이 생기면 지원한다.")
	ResponseEntity<ApiResponse<List<NoticeSummaryResponse>>> getNotices();

	@Operation(summary = "S2. 공지 상세")
	ResponseEntity<ApiResponse<NoticeResponse>> getNotice(Long noticeId);

	@Operation(summary = "S3. FAQ 목록·검색", description = "keyword는 질문·답변 부분 일치.")
	ResponseEntity<ApiResponse<List<FaqResponse>>> searchFaqs(String keyword);

	@Operation(summary = "S4. 1:1 문의 (미구현)")
	ResponseEntity<ApiResponse<Void>> submitInquiry(Jwt jwt, SubmitInquiryRequest request);
}
