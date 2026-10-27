package com.cusfit.cusfitbe.catalog.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "option_presets")
public class OptionPreset extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long brandId;

	@Column(nullable = false, columnDefinition = "text")
	private String name;

	protected OptionPreset() {
	}

	public static OptionPreset create(Long brandId, String name) {
		DomainValidation.requirePresent(brandId, "brandId");
		DomainValidation.requireText(name, "name");
		OptionPreset entity = new OptionPreset();
		entity.brandId = brandId;
		entity.name = name;
		return entity;
	}

	public Long getBrandId() {
		return brandId;
	}

	public String getName() {
		return name;
	}
}
