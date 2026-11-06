package com.cusfit.cusfitbe.recipe.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.global.response.PageResponse;
import com.cusfit.cusfitbe.recipe.dto.request.DuplicateCheckRequest;
import com.cusfit.cusfitbe.recipe.dto.request.MyRecipePageRequest;
import com.cusfit.cusfitbe.recipe.dto.request.RecipeSearchRequest;
import com.cusfit.cusfitbe.recipe.dto.response.DuplicateCheckResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeDetailResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeResponse;
import com.cusfit.cusfitbe.recipe.dto.response.RecipeSummaryResponse;
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

	// 아래 기능은 recipes의 상태·카운트 컬럼, 옵션 그룹, 좋아요·저장 테이블과 토큰 subject의 회원 연결(D1)이 생기면 구현한다.

	@Override
	public PageResponse<RecipeSummaryResponse> search(String authSubject, RecipeSearchRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public RecipeDetailResponse getDetail(String authSubject, Long recipeId) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public DuplicateCheckResponse checkDuplicate(String authSubject, DuplicateCheckRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public PageResponse<RecipeSummaryResponse> getMyRecipes(String authSubject, MyRecipePageRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}

	@Override
	public PageResponse<RecipeSummaryResponse> getSavedRecipes(String authSubject, MyRecipePageRequest request) {
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
