package com.cusfit.cusfitbe.support.dto.request;

import com.cusfit.cusfitbe.support.entity.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record SubmitInquiryRequest(@NotNull InquiryType type, @NotBlank @Size(max = 500) String content,
		@Size(max = 3) List<MultipartFile> images) {
}
