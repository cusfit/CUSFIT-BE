package com.cusfit.cusfitbe.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(@NotNull @AssertTrue Boolean agreed, Boolean deleteMyRecipes, String reason) {
}
