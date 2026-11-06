package com.cusfit.cusfitbe.catalog.dto.response;

import com.cusfit.cusfitbe.catalog.repository.MenuSearchProjection;
import java.time.Instant;

public record MenuSummaryResponse(Long id, Long brandId, String brandName, String name, Instant createdAt) {

	public static MenuSummaryResponse from(MenuSearchProjection projection) {
		return new MenuSummaryResponse(projection.getId(), projection.getBrandId(), projection.getBrandName(),
				projection.getName(), projection.getCreatedAt());
	}
}
