package com.cusfit.cusfitbe.report.dto.request;

import com.cusfit.cusfitbe.report.entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** content 필수 여부(D9)는 미정이라 길이만 제한한다. */
public record ReportRecipeRequest(@NotNull ReportReason reason, @Size(max = 200) String content) {
}
