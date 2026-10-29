package com.cusfit.cusfitbe.support.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "notices")
public class Notice extends BaseCreatedEntity {

	@Column(nullable = false, columnDefinition = "text")
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	protected Notice() {
	}

	public static Notice create(String title, String content) {
		DomainValidation.requireText(title, "title");
		DomainValidation.requireText(content, "content");
		Notice entity = new Notice();
		entity.title = title;
		entity.content = content;
		return entity;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
