package com.cusfit.cusfitbe.catalog.dto.response;

import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import java.time.Instant;

public record OptionPresetResponse(Long id, Long brandId, String name, Instant createdAt) {

	public static OptionPresetResponse from(OptionPreset entity) {
		return new OptionPresetResponse(entity.getId(), entity.getBrandId(), entity.getName(), entity.getCreatedAt());
	}
}
