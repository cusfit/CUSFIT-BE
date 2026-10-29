package com.cusfit.cusfitbe.recipe.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe_options")
public class RecipeOption extends BaseCreatedEntity {

	@Column(nullable = false, columnDefinition = "text")
	private String name;

	@Column(nullable = false)
	private int position;

	protected RecipeOption() {
	}

	static RecipeOption create(String name, int position) {
		DomainValidation.requireText(name, "name");
		RecipeOption option = new RecipeOption();
		option.name = name;
		option.position = position;
		return option;
	}

	public String getName() {
		return name;
	}

	public int getPosition() {
		return position;
	}
}
