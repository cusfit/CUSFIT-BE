package com.cusfit.cusfitbe.recipe.dto.request;

import com.cusfit.cusfitbe.recipe.entity.AiFeedbackRating;
import com.cusfit.cusfitbe.recipe.entity.AiFeedbackReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** DISLIKE일 때 reasons, reasons에 ETC가 있을 때 etcText가 필요하다. "건너뛰기" 처리(D8)는 미정이다. */
public record AiFeedbackRequest(@NotNull AiFeedbackRating rating, List<@NotNull AiFeedbackReason> reasons,
		@Size(max = 200) String etcText) {
}
