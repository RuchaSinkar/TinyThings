CREATE TABLE hydration_log (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                               log_date DATE NOT NULL,
                               slot_count INT NOT NULL DEFAULT 0,
                               UNIQUE (user_id, log_date)
);

CREATE TABLE user_streak (
                             user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
                             current_streak INT NOT NULL DEFAULT 0,
                             longest_streak INT NOT NULL DEFAULT 0,
                             last_active_date DATE
);