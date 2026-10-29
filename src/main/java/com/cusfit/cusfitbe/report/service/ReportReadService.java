package com.cusfit.cusfitbe.report.service;

import com.cusfit.cusfitbe.report.dto.response.ReportResponse;

public interface ReportReadService {

	ReportResponse getById(Long id);
}
