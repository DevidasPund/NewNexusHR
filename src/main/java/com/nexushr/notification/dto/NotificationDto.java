package com.nexushr.notification.dto;

import com.nexushr.common.enums.NotificationType;
import com.nexushr.notification.Notification;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String title,
        String body,
        NotificationType type,
        boolean read,
        boolean global,
        Instant createdAt) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getType(),
                n.isReadFlag(),
                n.getRecipientId() == null,
                n.getCreatedAt());
    }
}
