package com.tinythings.tinything;

import com.tinythings.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_tiny_thing_history")
@Getter
@Setter
@NoArgsConstructor
public class UserTinyThingHistory implements Persistable<UUID> {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "tiny_thing_id", nullable = false)
    private TinyThing tinyThing;

    @Column(name = "shown_at", nullable = false)
    private Instant shownAt;

    @Column(nullable = false)
    private boolean completed = false;

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