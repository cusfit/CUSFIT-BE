package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.dto.response.NoticeSummaryResponse;
import java.util.List;

public interface NoticeReadService {

	NoticeResponse getById(Long id);

	List<NoticeSummaryResponse> getAll();
}
