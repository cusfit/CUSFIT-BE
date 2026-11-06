package com.cusfit.cusfitbe.catalog.repository;

import com.cusfit.cusfitbe.catalog.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

	/** 빈 keyword는 모든 메뉴와 일치한다. 브랜드명은 같은 카탈로그 안의 Brand를 ID로 조인해 가져온다. */
	@Query("""
			select m.id as id, m.brandId as brandId, b.name as brandName, m.name as name, m.createdAt as createdAt
			from Menu m join Brand b on b.id = m.brandId
			where (:brandId is null or m.brandId = :brandId)
			and (lower(m.name) like lower(concat('%', :keyword, '%'))
				or lower(b.name) like lower(concat('%', :keyword, '%')))
			order by b.id, m.id
			""")
	List<MenuSearchProjection> search(@Param("brandId") Long brandId, @Param("keyword") String keyword);
}
