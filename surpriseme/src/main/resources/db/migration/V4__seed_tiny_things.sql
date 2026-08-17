-- Tags with fixed IDs so we can reference them below
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000001', 'coding');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000002', 'fitness');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000003', 'music');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000004', 'reading');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000005', 'mindfulness');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000006', 'productivity');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000007', 'cooking');
INSERT INTO tag (id, name) VALUES ('00000000-0000-0000-0000-000000000008', 'writing');

-- Tiny Things
INSERT INTO tiny_thing (id, title, description, category, time_of_day, difficulty) VALUES
                                                                                       ('10000000-0000-0000-0000-000000000001', 'Two-minute stretch', 'Stand up and stretch your arms, back, and neck for two minutes.', 'general', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000002', 'Solve one small bug', 'Pick the smallest open issue or bug on your list and just fix that one thing.', 'goal', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000003', 'Text someone thanks', 'Send a quick thank-you message to someone who helped you recently.', 'gratitude', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000004', '10 pushups or squats', 'A quick burst of movement — 10 pushups, squats, or jumping jacks.', 'general', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000005', 'Write one paragraph', 'Open your notes and write just one paragraph about anything on your mind.', 'goal', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000006', 'Three things you''re grateful for', 'Jot down three small things you''re grateful for today.', 'gratitude', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000007', 'Read one page', 'Pick up whatever book you''re reading and read just one page.', 'goal', 'evening', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000008', 'Play your favorite song', 'Put on a song you love and actually listen — no multitasking.', 'general', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000009', 'Tidy one small space', 'Clear off your desk, nightstand, or one shelf — just one spot.', 'general', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000010', 'Refactor one function', 'Find one messy function in your code and clean it up a little.', 'goal', 'any', 'medium'),
                                                                                       ('10000000-0000-0000-0000-000000000011', '5 minutes of quiet', 'Sit somewhere quiet for five minutes. No phone, no music, just breathe.', 'general', 'morning', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000012', 'Cook something new', 'Try one new ingredient or recipe step you haven''t before.', 'general', 'evening', 'medium'),
                                                                                       ('10000000-0000-0000-0000-000000000013', 'Message an old friend', 'Reach out to someone you haven''t talked to in a while.', 'gratitude', 'any', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000014', 'Plan tomorrow in 3 lines', 'Write three things you want to get done tomorrow.', 'goal', 'evening', 'easy'),
                                                                                       ('10000000-0000-0000-0000-000000000015', 'Learn one new word', 'Look up a word you don''t know and use it in a sentence today.', 'general', 'any', 'easy');

-- Tag associations
INSERT INTO tiny_thing_tag (tiny_thing_id, tag_id) VALUES
                                                       ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001'), -- bug -> coding
                                                       ('10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001'), -- refactor -> coding
                                                       ('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002'), -- pushups -> fitness
                                                       ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002'), -- stretch -> fitness
                                                       ('10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000003'), -- song -> music
                                                       ('10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000004'), -- read page -> reading
                                                       ('10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000005'), -- quiet -> mindfulness
                                                       ('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000006'), -- paragraph -> productivity
                                                       ('10000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000006'), -- plan tomorrow -> productivity
                                                       ('10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000006'), -- tidy -> productivity
                                                       ('10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000007'), -- cook -> cooking
                                                       ('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000008'); -- paragraph -> writing