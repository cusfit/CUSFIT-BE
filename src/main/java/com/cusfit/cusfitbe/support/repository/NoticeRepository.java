package com.cusfit.cusfitbe.support.repository;

import com.cusfit.cusfitbe.support.entity.Notice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	List<Notice> findAllByOrderByCreatedAtDescIdDesc();
}
