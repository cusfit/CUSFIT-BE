package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;

public interface NoticeReadService {

	NoticeResponse getById(Long id);
}
