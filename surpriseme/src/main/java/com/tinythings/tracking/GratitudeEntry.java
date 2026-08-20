package com.tinythings.tracking;

import com.tinythings.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gratitude_entry")
@Getter
@Setter
@NoArgsConstructor
public class GratitudeEntry implements Persistable<UUID> {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_type", nullable = false)
    private String entryType; // gratitude | thank_someone | message_someone

    @Column(length = 1000)
    private String content;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Transient
    private boolean isNew = true;

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