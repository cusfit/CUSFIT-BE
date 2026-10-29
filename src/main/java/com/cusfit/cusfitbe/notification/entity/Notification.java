package com.cusfit.cusfitbe.notification.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification extends BaseCreatedEntity {

	@Column(nullable = false)
	private Long recipientId;

	@Column(nullable = false, columnDefinition = "text")
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	protected Notification() {
	}

	public static Notification create(Long recipientId, String title, String content) {
		DomainValidation.requirePresent(recipientId, "recipientId");
		DomainValidation.requireText(title, "title");
		DomainValidation.requireText(content, "content");
		Notification entity = new Notification();
		entity.recipientId = recipientId;
		entity.title = title;
		entity.content = content;
		return entity;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
