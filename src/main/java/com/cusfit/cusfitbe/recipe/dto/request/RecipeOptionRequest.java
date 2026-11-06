package com.cusfit.cusfitbe.recipe.dto.request;

import com.cusfit.cusfitbe.recipe.entity.OptionGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 프리셋에서 고르지 않고 직접 입력한 옵션은 presetId가 null이다. 그룹별 최대 3개는 저장 시 검증한다. */
public record RecipeOptionRequest(@NotBlank @Size(max = 200) String name, @NotNull OptionGroup group, Long presetId,
		@Positive Integer quantity) {
}
