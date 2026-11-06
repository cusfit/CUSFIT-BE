package com.cusfit.cusfitbe.recipe.dto.request;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

/** 촬영·불러온 이미지 또는 자연어 설명 중 하나로 분석한다. 이미지는 저장하지 않는다 (R-10). */
public record AiAnalysisRequest(MultipartFile image, String text, Long brandId) {

	@AssertTrue(message = "image 또는 text 중 하나는 있어야 합니다")
	public boolean isImageOrTextPresent() {
		return (image != null && !image.isEmpty()) || (text != null && !text.isBlank());
	}
}
