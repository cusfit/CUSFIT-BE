package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.response.FaqResponse;

public interface FaqReadService {

	FaqResponse getById(Long id);
}
