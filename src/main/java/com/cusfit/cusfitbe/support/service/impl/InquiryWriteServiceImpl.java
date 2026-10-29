package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.support.dto.request.CreateInquiryRequest;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.entity.Inquiry;
import com.cusfit.cusfitbe.support.repository.InquiryRepository;
import com.cusfit.cusfitbe.support.service.InquiryWriteService;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InquiryWriteServiceImpl implements InquiryWriteService {

	private final InquiryRepository repository;
	private final UserReadService userReadService;

	public InquiryWriteServiceImpl(InquiryRepository repository, UserReadService userReadService) {
		this.repository = repository;
		this.userReadService = userReadService;
	}

	@Override
	public InquiryResponse create(CreateInquiryRequest request) {
		userReadService.getById(request.authorId());
		Inquiry entity = Inquiry.create(request.authorId(), request.title(), request.content());
		return InquiryResponse.from(repository.save(entity));
	}
}
