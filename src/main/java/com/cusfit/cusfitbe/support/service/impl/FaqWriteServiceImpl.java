package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.support.dto.request.CreateFaqRequest;
import com.cusfit.cusfitbe.support.dto.response.FaqResponse;
import com.cusfit.cusfitbe.support.entity.Faq;
import com.cusfit.cusfitbe.support.repository.FaqRepository;
import com.cusfit.cusfitbe.support.service.FaqWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FaqWriteServiceImpl implements FaqWriteService {

	private final FaqRepository repository;

	public FaqWriteServiceImpl(FaqRepository repository) {
		this.repository = repository;
	}

	@Override
	public FaqResponse create(CreateFaqRequest request) {
		Faq entity = Faq.create(request.question(), request.answer());
		return FaqResponse.from(repository.save(entity));
	}
}
