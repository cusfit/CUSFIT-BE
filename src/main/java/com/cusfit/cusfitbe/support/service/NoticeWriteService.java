package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;

public interface NoticeWriteService {

	NoticeResponse create(CreateNoticeRequest request);
}
