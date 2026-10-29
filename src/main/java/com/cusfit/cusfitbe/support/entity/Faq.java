package com.cusfit.cusfitbe.support.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "faqs")
public class Faq extends BaseCreatedEntity {

	@Column(nullable = false, columnDefinition = "text")
	private String question;

	@Column(nullable = false, columnDefinition = "text")
	private String answer;

	protected Faq() {
	}

	public static Faq create(String question, String answer) {
		DomainValidation.requireText(question, "question");
		DomainValidation.requireText(answer, "answer");
		Faq entity = new Faq();
		entity.question = question;
		entity.answer = answer;
		return entity;
	}

	public String getQuestion() {
		return question;
	}

	public String getAnswer() {
		return answer;
	}
}
