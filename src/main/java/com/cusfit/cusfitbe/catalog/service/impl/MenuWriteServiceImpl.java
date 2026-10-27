package com.cusfit.cusfitbe.catalog.service.impl;

import com.cusfit.cusfitbe.catalog.dto.request.CreateMenuRequest;
import com.cusfit.cusfitbe.catalog.dto.response.MenuResponse;
import com.cusfit.cusfitbe.catalog.entity.Menu;
import com.cusfit.cusfitbe.catalog.repository.MenuRepository;
import com.cusfit.cusfitbe.catalog.service.BrandReadService;
import com.cusfit.cusfitbe.catalog.service.MenuWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MenuWriteServiceImpl implements MenuWriteService {

	private final MenuRepository repository;
	private final BrandReadService brandReadService;

	public MenuWriteServiceImpl(MenuRepository repository, BrandReadService brandReadService) {
		this.repository = repository;
		this.brandReadService = brandReadService;
	}

	@Override
	public MenuResponse create(CreateMenuRequest request) {
		brandReadService.getById(request.brandId());
		Menu entity = Menu.create(request.brandId(), request.name());
		return MenuResponse.from(repository.save(entity));
	}
}
