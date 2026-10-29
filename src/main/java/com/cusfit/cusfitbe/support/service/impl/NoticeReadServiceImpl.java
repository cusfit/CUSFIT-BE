package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.repository.NoticeRepository;
import com.cusfit.cusfitbe.support.service.NoticeReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NoticeReadServiceImpl implements NoticeReadService {

	private final NoticeRepository repository;

	public NoticeReadServiceImpl(NoticeRepository repository) {
		this.repository = repository;
	}

	@Override
	public NoticeResponse getById(Long id) {
		return repository.findById(id)
				.map(NoticeResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
