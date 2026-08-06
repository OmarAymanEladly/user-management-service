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
        'Employee',
        'Employee user type',
        'ACTIVE',
        '[]'::jsonb
    ),
    (
        gen_random_uuid(),
        'Contractor',
        'Contractor user type',
        'ACTIVE',
        '[]'::jsonb
    ),
    (
        gen_random_uuid(),
        'Manager',
        'Manager user type',
        'ACTIVE',
        '[]'::jsonb
    ),
    (
        gen_random_uuid(),
        'Provisioned',
        'Provisioned user type',
        'ACTIVE',
        '[]'::jsonb
    );