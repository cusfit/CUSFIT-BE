package com.cusfit.cusfitbe.catalog.service;

import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;

public interface MenuWriteService {

	MenuResponse create(CreateMenuRequest request);
}
