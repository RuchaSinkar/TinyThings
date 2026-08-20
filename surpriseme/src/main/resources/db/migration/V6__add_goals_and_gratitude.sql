CREATE TABLE daily_goal (
                            id UUID PRIMARY KEY,
                            user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                            title VARCHAR(255) NOT NULL,
                            parent_goal_id UUID REFERENCES daily_goal(id) ON DELETE CASCADE,
                            completed BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at TIMESTAMP NOT NULL,
                            completed_at TIMESTAMP
);

CREATE INDEX idx_goal_user ON daily_goal(user_id);
CREATE INDEX idx_goal_parent ON daily_goal(parent_goal_id);

CREATE TABLE gratitude_entry (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                                 entry_type VARCHAR(50) NOT NULL,
                                 content VARCHAR(1000),
                                 completed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_gratitude_user ON gratitude_entry(user_id);