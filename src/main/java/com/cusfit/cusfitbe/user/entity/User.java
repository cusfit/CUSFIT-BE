package com.cusfit.cusfitbe.user.entity;

import com.cusfit.cusfitbe.global.entity.BaseCreatedEntity;
import com.cusfit.cusfitbe.global.validation.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseCreatedEntity {

	@Column(nullable = false, columnDefinition = "text")
	private String nickname;

	protected User() {
	}

	private User(String nickname) {
		this.nickname = nickname;
	}

	public static User create(String nickname) {
		DomainValidation.requireText(nickname, "nickname");
		return new User(nickname);
	}

	public String getNickname() {
		return nickname;
	}
}
