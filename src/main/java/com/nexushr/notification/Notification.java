package com.nexushr.notification;

import com.nexushr.common.BaseEntity;
import com.nexushr.common.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    /** Target employee id, or null for a global broadcast. */
    private Long recipientId;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.INFO;

    @Column(name = "is_read", nullable = false)
    private boolean readFlag = false;

    public Notification() {
    }

    public Notification(Long recipientId, String title, String body, NotificationType type) {
        this.recipientId = recipientId;
        this.title = title;
        this.body = body;
        this.type = type;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isReadFlag() {
        return readFlag;
    }

    public void setReadFlag(boolean readFlag) {
        this.readFlag = readFlag;
    }
}
