package com.cusfit.cusfitbe.report.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
public class Report extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long reporterId;

	@Column(nullable = false)
	private Long recipeId;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	protected Report() {
	}

	private Report(Long reporterId, Long recipeId, String content) {
		this.reporterId = reporterId;
		this.recipeId = recipeId;
		this.content = content;
	}

	public static Report create(Long reporterId, Long recipeId, String content) {
		DomainValidation.requirePresent(reporterId, "reporterId");
		DomainValidation.requirePresent(recipeId, "recipeId");
		DomainValidation.requireText(content, "content");
		return new Report(reporterId, recipeId, content);
	}

	public Long getReporterId() {
		return reporterId;
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public String getContent() {
		return content;
	}
}
