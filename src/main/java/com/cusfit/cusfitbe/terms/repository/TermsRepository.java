package com.cusfit.cusfitbe.terms.repository;

import com.cusfit.cusfitbe.terms.entity.Terms;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {

	List<Terms> findByEffectiveAtLessThanEqualOrderByTypeAscEffectiveAtDesc(Instant now);
}
