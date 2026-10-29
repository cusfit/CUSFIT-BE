package com.cusfit.cusfitbe.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@MappedSuperclass
public abstract class BaseCreatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Instant createdAt;

	protected BaseCreatedEntity() {
	}

	@PrePersist
	private void onCreate() {
		// timestamptz는 마이크로초까지만 저장하므로, 생성 응답과 재조회 값이 달라지지 않게 맞춘다.
		createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
	}

	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
