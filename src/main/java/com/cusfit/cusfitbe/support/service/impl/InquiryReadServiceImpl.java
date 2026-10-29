package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.response.InquiryResponse;
import com.cusfit.cusfitbe.support.repository.InquiryRepository;
import com.cusfit.cusfitbe.support.service.InquiryReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InquiryReadServiceImpl implements InquiryReadService {

	private final InquiryRepository repository;

	public InquiryReadServiceImpl(InquiryRepository repository) {
		this.repository = repository;
	}

	@Override
	public InquiryResponse getById(Long id) {
		return repository.findById(id)
				.map(InquiryResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
