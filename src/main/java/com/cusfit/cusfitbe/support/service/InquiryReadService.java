package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;

public interface InquiryReadService {

	InquiryResponse getById(Long id);
}
