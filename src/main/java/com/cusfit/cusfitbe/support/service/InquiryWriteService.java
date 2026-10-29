package com.cusfit.cusfitbe.support.service;

import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;

public interface InquiryWriteService {

	InquiryResponse create(CreateInquiryRequest request);
}
