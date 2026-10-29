package com.cusfit.cusfitbe.recipe.repository;

import com.cusfit.cusfitbe.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
