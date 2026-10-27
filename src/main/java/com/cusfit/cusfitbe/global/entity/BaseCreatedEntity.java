package com.cusfit.cusfitbe.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.Instant;

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
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
