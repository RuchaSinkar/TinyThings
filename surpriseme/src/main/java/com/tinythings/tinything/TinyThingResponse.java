package com.tinythings.tinything;

import java.util.List;
import java.util.UUID;

public record TinyThingResponse(
        UUID id,
        String title,
        String description,
        String category,
        List<String> tags
) {
    static TinyThingResponse from(TinyThing t) {
        return new TinyThingResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getCategory(),
                t.getTags().stream().map(tag -> tag.getName()).sorted().toList()
        );
    }
}