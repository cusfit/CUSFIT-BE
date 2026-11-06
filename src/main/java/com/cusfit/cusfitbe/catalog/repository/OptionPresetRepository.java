package com.cusfit.cusfitbe.catalog.repository;

import com.cusfit.cusfitbe.catalog.entity.OptionPreset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionPresetRepository extends JpaRepository<OptionPreset, Long> {

	List<OptionPreset> findByBrandIdOrderByIdAsc(Long brandId);
}
