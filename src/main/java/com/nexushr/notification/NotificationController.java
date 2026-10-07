package com.nexushr.notification;

import com.nexushr.notification.dto.NotificationDto;
import com.nexushr.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationDto> mine(@AuthenticationPrincipal UserPrincipal me) {
        return service.listForUser(me.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserPrincipal me) {
        return Map.of("count", service.unreadCount(me.getId()));
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        service.markRead(id);
    }

    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal UserPrincipal me) {
        service.markAllRead(me.getId());
    }
}
