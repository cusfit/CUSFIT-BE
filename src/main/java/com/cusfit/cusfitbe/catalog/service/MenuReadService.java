package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;

public interface MenuReadService {

	MenuResponse getById(Long id);
}
