package com.cusfit.cusfitbe.catalog.repository;

import java.time.Instant;

public interface MenuSearchProjection {

	Long getId();

	Long getBrandId();

	String getBrandName();

	String getName();

	Instant getCreatedAt();
}
