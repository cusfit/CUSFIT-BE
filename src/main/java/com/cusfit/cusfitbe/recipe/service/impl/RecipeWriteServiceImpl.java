package com.cusfit.cusfitbe.recipe.service.impl;

import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeOptionRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.entity.Recipe;
import com.cusfit.cusfitbe.recipe.repository.RecipeRepository;
import com.cusfit.cusfitbe.recipe.service.RecipeWriteService;
import com.cusfit.cusfitbe.user.service.UserReadService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecipeWriteServiceImpl implements RecipeWriteService {

	private final RecipeRepository repository;
	private final UserReadService userReadService;
	private final MenuReadService menuReadService;

	public RecipeWriteServiceImpl(RecipeRepository repository, UserReadService userReadService,
			MenuReadService menuReadService) {
		this.repository = repository;
		this.userReadService = userReadService;
		this.menuReadService = menuReadService;
	}

	@Override
	public RecipeResponse create(CreateRecipeRequest request) {
		userReadService.getById(request.authorId());
		menuReadService.getById(request.menuId());
		List<String> optionNames = request.options().stream().map(CreateRecipeOptionRequest::name).toList();
		Recipe recipe = Recipe.create(request.authorId(), request.menuId(), request.title(), optionNames);
		return RecipeResponse.from(repository.save(recipe));
	}
}
