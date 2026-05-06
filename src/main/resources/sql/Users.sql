-- Create enum type first
CREATE TYPE user_status AS ENUM ('PENDING', ACTIVE', 'INACTIVE', 'BLOCKED');

-- Create table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    aadhaar VARCHAR(20),
    email VARCHAR(30),
    status user_status DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- INSERT records to Users table
INSERT INTO users (name, phone, aadhaar, email)
VALUES ('Ravi Kumar', '9876543210', '1234-5678-9012', 'ravi@example.com');
INSERT INTO users (name, phone, aadhaar, email)
VALUES ('Anita Sharma', '9123456780', '2345-6789-0123', 'anita@example.com');
INSERT INTO users (name, phone, aadhaar, email, status)
VALUES ('Rahul Verma', '9988776655', '3456-7890-1234', 'rahul@example.com', 'INACTIVE');
INSERT INTO users (name, phone)
VALUES ('Kiran Rao', '9012345678');
INSERT INTO users (name, phone, aadhaar, email, status)
VALUES ('Sneha Reddy', '9090909090', '4567-8901-2345', 'sneha@example.com', 'BLOCKED');


