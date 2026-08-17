CREATE TABLE app_user (
                          id UUID PRIMARY KEY,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          last_action_at TIMESTAMP
);

CREATE TABLE user_profile (
                              user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
                              name VARCHAR(255)
);

CREATE TABLE robot_state (
                             user_id UUID PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
                             mood VARCHAR(50) NOT NULL DEFAULT 'idle'
);