package com.cusfit.cusfitbe.support.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cusfit.cusfitbe.global.config.ApiSecurityConfig;
import com.cusfit.cusfitbe.support.fixture.SupportFixture;
import com.cusfit.cusfitbe.support.service.FaqWriteService;
import com.cusfit.cusfitbe.support.service.NoticeWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = SupportAdminController.class)
@ActiveProfiles("test")
@Import(ApiSecurityConfig.class)
class SupportAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NoticeWriteService noticeWriteService;

	@MockitoBean
	private FaqWriteService faqWriteService;

	@Test
	@DisplayName("관리자가 공지를 등록하면 201과 등록된 공지를 반환한다")
	void createNotice() throws Exception {
		// given
		given(noticeWriteService.create(SupportFixture.createNoticeRequest())).willReturn(SupportFixture.noticeResponse());

		// when
		ResultActions result = postAsAdmin("/api/v1/admin/notices",
				SupportFixture.noticeJson(SupportFixture.NOTICE_TITLE, SupportFixture.NOTICE_CONTENT));

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(SupportFixture.NOTICE_ID));
	}

	@Test
	@DisplayName("관리자가 FAQ를 등록하면 201과 등록된 FAQ를 반환한다")
	void createFaq() throws Exception {
		// given
		given(faqWriteService.create(SupportFixture.createFaqRequest())).willReturn(SupportFixture.faqResponse());

		// when
		ResultActions result = postAsAdmin("/api/v1/admin/faqs",
				SupportFixture.faqJson(SupportFixture.FAQ_QUESTION, SupportFixture.FAQ_ANSWER));

		// then
		result.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(SupportFixture.FAQ_ID));
	}

	private ResultActions postAsAdmin(String path, String body) throws Exception {
		return mockMvc.perform(post(path)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}
}
