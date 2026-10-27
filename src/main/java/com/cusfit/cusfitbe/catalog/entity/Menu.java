package com.cusfit.cusfitbe.catalog.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "menus")
public class Menu extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long brandId;

	@Column(nullable = false, columnDefinition = "text")
	private String name;

	protected Menu() {
	}

	public static Menu create(Long brandId, String name) {
		DomainValidation.requirePresent(brandId, "brandId");
		DomainValidation.requireText(name, "name");
		Menu entity = new Menu();
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
