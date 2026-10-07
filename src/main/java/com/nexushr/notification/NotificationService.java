package com.nexushr.notification;

import com.nexushr.common.enums.NotificationType;
import com.nexushr.notification.dto.NotificationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> listForUser(Long userId) {
        return repo.findForRecipient(userId).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repo.countUnread(userId);
    }

    @Transactional
    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            repo.save(n);
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> items = repo.findForRecipient(userId);
        for (Notification n : items) {
            n.setReadFlag(true);
        }
        repo.saveAll(items);
    }

    @Transactional
    public Notification push(Long recipientId, String title, String body, NotificationType type) {
        return repo.save(new Notification(recipientId, title, body, type));
    }
}
