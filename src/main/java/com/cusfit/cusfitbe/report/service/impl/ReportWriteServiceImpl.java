package com.cusfit.cusfitbe.report.service.impl;

import com.cusfit.cusfitbe.recipe.service.RecipeReadService;
import com.cusfit.cusfitbe.report.dto.request.CreateReportRequest;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.entity.Report;
import com.cusfit.cusfitbe.report.repository.ReportRepository;
import com.cusfit.cusfitbe.report.service.ReportWriteService;
import com.cusfit.cusfitbe.user.service.UserReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportWriteServiceImpl implements ReportWriteService {

	private final ReportRepository reportRepository;
	private final UserReadService userReadService;
	private final RecipeReadService recipeReadService;

	public ReportWriteServiceImpl(ReportRepository reportRepository, UserReadService userReadService,
			RecipeReadService recipeReadService) {
		this.reportRepository = reportRepository;
		this.userReadService = userReadService;
		this.recipeReadService = recipeReadService;
	}

	@Override
	public ReportResponse create(CreateReportRequest request) {
		userReadService.getById(request.reporterId());
		recipeReadService.getById(request.recipeId());
		Report report = Report.create(request.reporterId(), request.recipeId(), request.content());
		return ReportResponse.from(reportRepository.save(report));
	}
}
