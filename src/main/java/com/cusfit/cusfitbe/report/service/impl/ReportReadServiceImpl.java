package com.cusfit.cusfitbe.report.service.impl;

import com.cusfit.cusfitbe.global.exception.BusinessException;
import com.cusfit.cusfitbe.global.exception.ErrorCode;
import com.cusfit.cusfitbe.report.dto.response.ReportResponse;
import com.cusfit.cusfitbe.report.repository.ReportRepository;
import com.cusfit.cusfitbe.report.service.ReportReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportReadServiceImpl implements ReportReadService {

	private final ReportRepository reportRepository;

	public ReportReadServiceImpl(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}

	@Override
	public ReportResponse getById(Long id) {
		return reportRepository.findById(id)
				.map(ReportResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
	}
}
