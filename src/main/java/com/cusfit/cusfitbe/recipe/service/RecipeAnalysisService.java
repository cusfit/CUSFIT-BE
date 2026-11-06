package com.cusfit.cusfitbe.recipe.service;

import com.cusfit.cusfitbe.recipe.dto.request.AiAnalysisRequest;
import com.cusfit.cusfitbe.recipe.dto.response.AiAnalysisResponse;

public interface RecipeAnalysisService {

	AiAnalysisResponse analyze(String authSubject, AiAnalysisRequest request);
}
