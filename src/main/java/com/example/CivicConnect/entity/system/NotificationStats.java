package com.example.CivicConnect.entity.system;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks notification statistics for each user
 * This entity maintains counts to avoid expensive queries on every dashboard load
 */
@Entity
@Table(name = "notification_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Long totalNotifications = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long unreadCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long unseenCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increment counts when a new notification is created
     */
    public void incrementCounts() {
        this.totalNotifications++;
        this.unreadCount++;
        this.unseenCount++;
    }

    /**
     * Decrement unread count when notification is marked as read
     */
    public void decrementUnreadCount() {
        if (this.unreadCount > 0) {
            this.unreadCount--;
        }
    }

    /**
     * Decrement unseen count when notification is marked as seen
     */
    public void decrementUnseenCount() {
        if (this.unseenCount > 0) {
            this.unseenCount--;
        }
    }

    /**
     * Reset unread count to zero (when marking all as read)
     */
    public void resetUnreadCount() {
        this.unreadCount = 0L;
    }

    /**
     * Reset unseen count to zero (when marking all as seen)
     */
    public void resetUnseenCount() {
        this.unseenCount = 0L;
    }
}
