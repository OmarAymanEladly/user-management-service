-- 1. Table for User Types
CREATE TABLE user_types (
                            id uuid PRIMARY KEY,
                            type VARCHAR(50) NOT NULL UNIQUE,
                            description TEXT,
                            status VARCHAR(20) DEFAULT 'ACTIVE',

    -- This stores the list of fields: [ {fieldName: 'companyName', required: true...}, {...} ]
                            fields JSONB NOT NULL,

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
