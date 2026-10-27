package com.cusfit.cusfitbe.catalog.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "brands")
public class Brand extends BaseCreatedEntity {

	@Column(nullable = false, columnDefinition = "text")
	private String name;

	protected Brand() {
	}

	public static Brand create(String name) {
		DomainValidation.requireText(name, "name");
		Brand entity = new Brand();
		entity.name = name;
		return entity;
	}

	public String getName() {
		return name;
	}
}
