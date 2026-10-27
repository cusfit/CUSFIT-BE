package com.cusfit.cusfitbe.terms.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "terms")
public class Terms extends BaseCreatedEntity {

	@Column(columnDefinition = "text", nullable = false)
	private String type;

	@Column(columnDefinition = "text", nullable = false)
	private String version;

	@Column(columnDefinition = "text", nullable = false)
	private String contentUrl;

	@Column(nullable = false)
	private Instant effectiveAt;

	protected Terms() {
	}

	private Terms(String type, String version, String contentUrl, Instant effectiveAt) {
		this.type = type;
		this.version = version;
		this.contentUrl = contentUrl;
		this.effectiveAt = effectiveAt;
	}

	public static Terms create(String type, String version, String contentUrl, Instant effectiveAt) {
		DomainValidation.requireText(type, "type");
		DomainValidation.requireText(version, "version");
		DomainValidation.requireText(contentUrl, "contentUrl");
		DomainValidation.requirePresent(effectiveAt, "effectiveAt");
		return new Terms(type, version, contentUrl, effectiveAt);
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public Instant getEffectiveAt() {
		return effectiveAt;
	}
}
