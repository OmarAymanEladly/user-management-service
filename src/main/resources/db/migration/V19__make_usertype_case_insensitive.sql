
ALTER TABLE user_types DROP CONSTRAINT IF EXISTS user_types_type_key;


CREATE UNIQUE INDEX idx_user_types_type_case_insensitive ON user_types (LOWER(type));