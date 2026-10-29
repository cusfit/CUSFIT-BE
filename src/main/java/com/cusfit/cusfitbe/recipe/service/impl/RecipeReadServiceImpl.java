package com.cusfit.cusfitbe.recipe.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.repository.RecipeRepository;
import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecipeReadServiceImpl implements RecipeReadService {

	private final RecipeRepository repository;

	public RecipeReadServiceImpl(RecipeRepository repository) {
		this.repository = repository;
	}

	@Override
	public RecipeResponse getById(Long id) {
		return repository.findById(id)
				.map(RecipeResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
