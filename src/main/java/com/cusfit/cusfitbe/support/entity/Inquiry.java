package com.cusfit.cusfitbe.support.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "inquiries")
public class Inquiry extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long authorId;

	@Column(nullable = false, columnDefinition = "text")
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	protected Inquiry() {
	}

	public static Inquiry create(Long authorId, String title, String content) {
		DomainValidation.requirePresent(authorId, "authorId");
		DomainValidation.requireText(title, "title");
		DomainValidation.requireText(content, "content");
		Inquiry entity = new Inquiry();
		entity.authorId = authorId;
		entity.title = title;
		entity.content = content;
		return entity;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
