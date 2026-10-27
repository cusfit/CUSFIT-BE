package com.cusfit.cusfitbe.terms.repository;

import com.cusfit.cusfitbe.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
