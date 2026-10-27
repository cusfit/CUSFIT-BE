package com.cusfit.cusfitbe.user.repository;

import com.cusfit.cusfitbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
