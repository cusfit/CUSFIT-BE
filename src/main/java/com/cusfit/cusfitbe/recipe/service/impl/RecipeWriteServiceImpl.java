package com.cusfit.cusfitbe.recipe.service.impl;

import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.request.AiFeedbackRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeOptionRequest;
import com.cusfit.cusfitbe.recipe.dto.request.CreateRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RegisterRecipeRequest;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeDetailResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeLikeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSaveResponse;
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

	// 아래 기능은 토큰 subject의 회원 연결(D1), recipes 상태·카운트·생성 방식 컬럼, 옵션 그룹,
	// 좋아요·저장·AI 피드백 테이블이 생기면 구현한다.

	@Override
	public RecipeDetailResponse register(String authSubject, RegisterRecipeRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public void delete(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public void submitAiFeedback(String authSubject, Long recipeId, AiFeedbackRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public RecipeLikeResponse like(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public RecipeLikeResponse unlike(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public RecipeSaveResponse save(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public RecipeSaveResponse unsave(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
