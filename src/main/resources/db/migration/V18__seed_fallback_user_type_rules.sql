-- LDAP Fallback Rule

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
    'FALLBACK',
    '*',
    '*',
    'LDAP',
    0,
    id,
    TRUE
FROM user_types
WHERE type = 'Provisioned';

-- IDENTITY_PROVIDER Fallback Rule

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
    'FALLBACK',
    '*',
    '*',
    'IDENTITY_PROVIDER',
    0,
    id,
    TRUE
FROM user_types
WHERE type = 'Provisioned';

-- ADMIN Fallback Rule

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
    'FALLBACK',
    '*',
    '*',
    'ADMIN',
    0,
    id,
    TRUE
FROM user_types
WHERE type = 'Provisioned';

