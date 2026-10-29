package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;

public interface FaqWriteService {

	FaqResponse create(CreateFaqRequest request);
}
