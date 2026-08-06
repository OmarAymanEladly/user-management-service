-- Temp Dev Role for Now
INSERT INTO user_types (
    id,
    type,
    description,
    status,
    fields
)
VALUES
    (
        gen_random_uuid(),
        'Developer',
        'Developer user type',
        'ACTIVE',
        '[]'::jsonb
    );

-- IDENTITY_PROVIDER Rules

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Employee',
    'IDENTITY_PROVIDER',
    200,
    id,
    TRUE
FROM user_types
WHERE type = 'Employee';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Contractor',
    'IDENTITY_PROVIDER',
    210,
    id,
    TRUE
FROM user_types
WHERE type = 'Contractor';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Manager',
    'IDENTITY_PROVIDER',
    220,
    id,
    TRUE
FROM user_types
WHERE type = 'Manager';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'DEVELOPER',
    'IDENTITY_PROVIDER',
    230,
    id,
    TRUE
FROM user_types
WHERE type = 'Developer';

-- ADMIN Rules

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Employee',
    'ADMIN',
    300,
    id,
    TRUE
FROM user_types
WHERE type = 'Employee';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Contractor',
    'ADMIN',
    310,
    id,
    TRUE
FROM user_types
WHERE type = 'Contractor';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'Manager',
    'ADMIN',
    320,
    id,
    TRUE
FROM user_types
WHERE type = 'Manager';

INSERT INTO user_type_rules (
    id,
    evaluator_type,
    match_key,
    match_value,
    applicable_source,
    priority,
    user_type_id,
    active
)
SELECT
    gen_random_uuid(),
    'ATTRIBUTE',
    'user_type',
    'DEVELOPER',
    'ADMIN',
    330,
    id,
    TRUE
FROM user_types
WHERE type = 'Developer';