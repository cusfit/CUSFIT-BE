package com.cusfit.cusfitbe.recipe.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.recipe.dto.request.AiAnalysisRequest;
import com.cusfit.cusfitbe.recipe.dto.response.AiAnalysisResponse;
import com.cusfit.cusfitbe.recipe.service.RecipeAnalysisService;
import org.springframework.stereotype.Service;

@Service
public class RecipeAnalysisServiceImpl implements RecipeAnalysisService {

	@Override
	public AiAnalysisResponse analyze(String authSubject, AiAnalysisRequest request) {
		// AI 공급자, 하루 횟수 제한(R-03), 분석 실패 코드(D13)가 정해지면 구현한다.
		throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
	}
}
