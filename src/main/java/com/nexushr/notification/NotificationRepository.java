package com.nexushr.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.recipientId = :rid or n.recipientId is null "
            + "order by n.createdAt desc")
    List<Notification> findForRecipient(@Param("rid") Long rid);

    @Query("select count(n) from Notification n where (n.recipientId = :rid or n.recipientId is null) "
            + "and n.readFlag = false")
    long countUnread(@Param("rid") Long rid);
}
