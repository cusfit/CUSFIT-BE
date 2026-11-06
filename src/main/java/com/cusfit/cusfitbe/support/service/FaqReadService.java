package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import java.util.List;

public interface FaqReadService {

	FaqResponse getById(Long id);

	List<FaqResponse> search(String keyword);
}
