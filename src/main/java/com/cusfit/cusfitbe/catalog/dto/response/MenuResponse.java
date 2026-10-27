package com.cusfit.cusfitbe.catalog.dto.response;

import com.cusfit.cusfitbe.catalog.entity.Menu;
import java.time.Instant;

public record MenuResponse(Long id, Long brandId, String name, Instant createdAt) {

	public static MenuResponse from(Menu entity) {
		return new MenuResponse(entity.getId(), entity.getBrandId(), entity.getName(), entity.getCreatedAt());
	}
}
