package com.cusfit.cusfitbe.support.service.impl;

import com.cusfit.cusfitbe.support.dto.request.CreateNoticeRequest;
import com.cusfit.cusfitbe.support.dto.response.NoticeResponse;
import com.cusfit.cusfitbe.support.entity.Notice;
import com.cusfit.cusfitbe.support.repository.NoticeRepository;
import com.cusfit.cusfitbe.support.service.NoticeWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoticeWriteServiceImpl implements NoticeWriteService {

	private final NoticeRepository repository;

	public NoticeWriteServiceImpl(NoticeRepository repository) {
		this.repository = repository;
	}

	@Override
	public NoticeResponse create(CreateNoticeRequest request) {
		Notice entity = Notice.create(request.title(), request.content());
		return NoticeResponse.from(repository.save(entity));
	}
}
