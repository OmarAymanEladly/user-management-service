-- RDN Rules

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
    'RDN',
    'ou',
    'Employees',
    'LDAP',
    100,
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
    'RDN',
    'ou',
    'Contractors',
    'LDAP',
    90,
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
    'RDN',
    'ou',
    'Management',
    'LDAP',
    80,
    id,
    TRUE
FROM user_types
WHERE type = 'Manager';


-- Attribute Rules

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
    'employeeType',
    'Employee',
    'LDAP',
    50,
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
    'employeeType',
    'Contractor',
    'LDAP',
    40,
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
    'employeeType',
    'Manager',
    'LDAP',
    30,
    id,
    TRUE
FROM user_types
WHERE type = 'Manager';