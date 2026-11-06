package com.cusfit.cusfitbe.support.repository;

import com.cusfit.cusfitbe.support.entity.Faq;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FaqRepository extends JpaRepository<Faq, Long> {

	/** 빈 keyword는 모든 FAQ와 일치한다. */
	@Query("""
			select f from Faq f
			where lower(f.question) like lower(concat('%', :keyword, '%'))
			or lower(f.answer) like lower(concat('%', :keyword, '%'))
			order by f.id
			""")
	List<Faq> search(@Param("keyword") String keyword);
}
