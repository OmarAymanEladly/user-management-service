CREATE TABLE user_type_rules (
    id UUID PRIMARY KEY,

    evaluator_type VARCHAR(50) NOT NULL,
    match_key VARCHAR(100) NOT NULL,
    match_value VARCHAR(255) NOT NULL,

    applicable_source VARCHAR(50) NOT NULL,

    priority INTEGER NOT NULL,

    user_type_id UUID NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_user_type_rules_user_type
        FOREIGN KEY (user_type_id) REFERENCES user_types(id)
);