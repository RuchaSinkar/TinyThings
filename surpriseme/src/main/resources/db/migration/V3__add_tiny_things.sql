CREATE TABLE tiny_thing (
                            id UUID PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            description VARCHAR(1000) NOT NULL,
                            category VARCHAR(50) NOT NULL,
                            time_of_day VARCHAR(20) NOT NULL DEFAULT 'any',
                            difficulty VARCHAR(20) NOT NULL DEFAULT 'easy'
);

CREATE TABLE tiny_thing_tag (
                                tiny_thing_id UUID NOT NULL REFERENCES tiny_thing(id) ON DELETE CASCADE,
                                tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
                                PRIMARY KEY (tiny_thing_id, tag_id)
);

CREATE TABLE user_tiny_thing_history (
                                         id UUID PRIMARY KEY,
                                         user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                         tiny_thing_id UUID NOT NULL REFERENCES tiny_thing(id) ON DELETE CASCADE,
                                         shown_at TIMESTAMP NOT NULL,
                                         completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_history_user_shown ON user_tiny_thing_history(user_id, shown_at);