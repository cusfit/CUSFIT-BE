package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import java.util.List;

public interface MenuReadService {

	MenuResponse getById(Long id);

	List<MenuSummaryResponse> search(Long brandId, String keyword);
}
