ALTER TABLE user_profile ADD COLUMN role VARCHAR(50);
ALTER TABLE user_profile ADD COLUMN field VARCHAR(255);
ALTER TABLE user_profile ADD COLUMN focus_areas VARCHAR(500);
ALTER TABLE user_profile ADD COLUMN active_hours_start VARCHAR(5);
ALTER TABLE user_profile ADD COLUMN active_hours_end VARCHAR(5);
ALTER TABLE user_profile ADD COLUMN goals_text VARCHAR(1000);
ALTER TABLE user_profile ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE tag (
                     id UUID PRIMARY KEY,
                     name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE user_tag (
                          user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                          tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
                          PRIMARY KEY (user_id, tag_id)
);