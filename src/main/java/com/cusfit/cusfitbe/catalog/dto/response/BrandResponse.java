package com.cusfit.cusfitbe.catalog.dto.response;

import com.cusfit.cusfitbe.catalog.entity.Brand;
import java.time.Instant;

public record BrandResponse(Long id, String name, Instant createdAt) {

	public static BrandResponse from(Brand entity) {
		return new BrandResponse(entity.getId(), entity.getName(), entity.getCreatedAt());
	}
}
