-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20) UNIQUE NOT NULL,
    aadhaar VARCHAR(12),
    email VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_users_updated_at 
BEFORE UPDATE ON users 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Address Table
CREATE TABLE IF NOT EXISTS address (
    id BIGSERIAL PRIMARY KEY,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_address_updated_at 
BEFORE UPDATE ON address 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Amenity Table
CREATE TABLE IF NOT EXISTS amenity (
    id SERIAL PRIMARY KEY,
    amenity_name VARCHAR(50) UNIQUE NOT NULL
);

-- Vehicle Type Table
CREATE TABLE IF NOT EXISTS vehicle_type (
    id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) UNIQUE NOT NULL
);

-- Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type_id INT NOT NULL REFERENCES vehicle_type(id),
    vehicle_make VARCHAR(100),
    vehicle_number VARCHAR(20) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_vehicles_updated_at 
BEFORE UPDATE ON vehicles 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Parking Table
CREATE TABLE IF NOT EXISTS parking (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users(id),
    address_id BIGINT REFERENCES address(id),
    description TEXT,
    emergency_contact VARCHAR(20),
    is_open_24_hours BOOLEAN DEFAULT FALSE,
    approval_required BOOLEAN DEFAULT FALSE,
    start_time TIME,
    end_time TIME,
    ad_start_date DATE NOT NULL,
    ad_end_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_parking_updated_at 
BEFORE UPDATE ON parking 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Parking Images Table
CREATE TABLE IF NOT EXISTS parking_images (
    id BIGSERIAL PRIMARY KEY,
    parking_id BIGINT NOT NULL REFERENCES parking(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_parking_images_updated_at 
BEFORE UPDATE ON parking_images 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Parking Vehicle Configs Table
CREATE TABLE IF NOT EXISTS parking_vehicle_configs (
    id BIGSERIAL PRIMARY KEY,
    parking_id BIGINT NOT NULL REFERENCES parking(id) ON DELETE CASCADE,
    vehicle_type_id INT NOT NULL REFERENCES vehicle_type(id),
    max_capacity INT NOT NULL DEFAULT 0 CHECK (max_capacity >= 0),
    hourly_rate NUMERIC(10, 2) DEFAULT 0.00,
    daily_rate NUMERIC(10, 2) DEFAULT 0.00,
    weekly_rate NUMERIC(10, 2) DEFAULT 0.00,
    monthly_rate NUMERIC(10, 2) DEFAULT 0.00,
    UNIQUE(parking_id, vehicle_type_id)
);

-- Parking Availability Days (ElementCollection)
CREATE TABLE IF NOT EXISTS parking_availability_days (
    parking_id BIGINT NOT NULL REFERENCES parking(id) ON DELETE CASCADE,
    day_of_week VARCHAR(50) NOT NULL
);

-- Parking Amenities Mapping (ManyToMany)
CREATE TABLE IF NOT EXISTS parking_amenities_mapping (
    parking_id BIGINT NOT NULL REFERENCES parking(id) ON DELETE CASCADE,
    amenity_id INT NOT NULL REFERENCES amenity(id),
    PRIMARY KEY (parking_id, amenity_id)
);

-- Pricing Table
CREATE TABLE IF NOT EXISTS pricing (
    id BIGSERIAL PRIMARY KEY,
    parking_id BIGINT NOT NULL REFERENCES parking(id) ON DELETE CASCADE,
    vehicle_type_id INT NOT NULL REFERENCES vehicle_type(id),
    pricing_type VARCHAR(50) NOT NULL, -- e.g., HOURLY, DAILY, WEEKLY, MONTHLY
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_pricing_updated_at 
BEFORE UPDATE ON pricing 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- notifications table
-- bookings table
-- payment table
-- review table
-- support ticket table

-- Master Data Inserts
INSERT INTO vehicle_type (type_name) VALUES 
('Two Wheeler'), 
('Four Wheeler')
ON CONFLICT (type_name) DO NOTHING;

INSERT INTO amenity (amenity_name) VALUES 
('CCTV Camera'), 
('Security Guard'), 
('Covered Parking'), 
('Charging Point'), 
('Wheelchair Accessible') 
ON CONFLICT (amenity_name) DO NOTHING;
