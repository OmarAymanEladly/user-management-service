ALTER TABLE user_type_rules
DROP CONSTRAINT fk_user_type_rules_user_type;


ALTER TABLE user_type_rules
    ADD CONSTRAINT fk_user_type_rules_user_type
        FOREIGN KEY (user_type_id)
            REFERENCES user_types(id)
            ON DELETE CASCADE;