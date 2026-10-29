package com.cusfit.cusfitbe.recipe.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long authorId;

	@Column(nullable = false)
	private Long menuId;

	@Column(nullable = false, columnDefinition = "text")
	private String title;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "recipe_id", nullable = false)
	@OrderBy("position ASC")
	private List<RecipeOption> options = new ArrayList<>();

	protected Recipe() {
	}

	public static Recipe create(Long authorId, Long menuId, String title, List<String> optionNames) {
		DomainValidation.requirePresent(authorId, "authorId");
		DomainValidation.requirePresent(menuId, "menuId");
		DomainValidation.requireText(title, "title");
		DomainValidation.requirePresent(optionNames, "options");
		Recipe recipe = new Recipe();
		recipe.authorId = authorId;
		recipe.menuId = menuId;
		recipe.title = title;
		for (int i = 0; i < optionNames.size(); i++) {
			recipe.options.add(RecipeOption.create(optionNames.get(i), i));
		}
		return recipe;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public Long getMenuId() {
		return menuId;
	}

	public String getTitle() {
		return title;
	}

	public List<RecipeOption> getOptions() {
		return List.copyOf(options);
	}
}
