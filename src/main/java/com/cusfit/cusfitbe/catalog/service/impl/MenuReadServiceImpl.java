package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.dto.response.MenuSummaryResponse;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.catalog.service.MenuReadService;
import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuReadServiceImpl implements MenuReadService {

	private final MenuRepository repository;

	public MenuReadServiceImpl(MenuRepository repository) {
		this.repository = repository;
	}

	@Override
	public MenuResponse getById(Long id) {
		return repository.findById(id)
				.map(MenuResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}

	@Override
	public List<MenuSummaryResponse> search(Long brandId, String keyword) {
		String normalizedKeyword = keyword == null ? "" : keyword.strip();
		return repository.search(brandId, normalizedKeyword).stream().map(MenuSummaryResponse::from).toList();
	}
}
