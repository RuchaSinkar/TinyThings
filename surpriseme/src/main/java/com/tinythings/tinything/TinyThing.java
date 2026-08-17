package com.tinythings.tinything;

import com.tinythings.user.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tiny_thing")
@Getter
@Setter
@NoArgsConstructor
public class TinyThing implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String category; // hydration | gratitude | goal | general

    @Column(name = "time_of_day", nullable = false)
    private String timeOfDay = "any"; // morning | afternoon | evening | any

    @Column(nullable = false)
    private String difficulty = "easy"; // easy | medium

    @ManyToMany
    @JoinTable(
            name = "tiny_thing_tag",
            joinColumns = @JoinColumn(name = "tiny_thing_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

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