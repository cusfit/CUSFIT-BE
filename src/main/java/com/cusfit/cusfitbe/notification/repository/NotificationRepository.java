package com.cusfit.cusfitbe.notification.repository;

import com.cusfit.cusfitbe.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
