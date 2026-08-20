package com.tinythings.tracking;

import com.tinythings.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_streak")
@Getter
@Setter
@NoArgsConstructor
public class UserStreak implements Persistable<UUID> {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}