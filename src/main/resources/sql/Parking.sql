CREATE TABLE vehicle_types (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_name VARCHAR(50) UNIQUE NOT NULL
); 

insert into Vehicle_types (type_name) values ('TWO_WHEELER'), ('FOUR_WHEELER');


CREATE TABLE address (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),

    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE listing_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE parking_listings (
    id int8 GENERATED ALWAYS AS IDENTITY,
    owner_id BIGINT NOT NULL,
    
    -- Location Details
    address_id BIGINT,
    
    -- About Parking
    description TEXT,
    
    -- Contact Details
    emergency_contact VARCHAR(20),
    
    -- Availability & Duration
    is_open_24_hours BOOLEAN DEFAULT FALSE,
    start_time TIME,
    end_time TIME,
    ad_start_date DATE NOT NULL,
    ad_end_date DATE NOT NULL,
    
    status listing_status DEFAULT 'ACTIVE',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_parking_listings PRIMARY KEY (id),
    CONSTRAINT fk_listing_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_listing_address FOREIGN KEY (address_id) REFERENCES address(id)
);

CREATE TYPE day_of_week_enum AS ENUM (
    'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'
);

CREATE TABLE parking_availability_days (
    parking_id BIGINT NOT NULL,
    day_of_week day_of_week_enum NOT NULL,
    
    PRIMARY KEY (parking_id, day_of_week),
   
    CONSTRAINT fk_pad_parking 
        FOREIGN KEY (parking_id) 
        REFERENCES parking_listings(id));
    
CREATE TABLE facilities (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    facility_name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO facilities (facility_name) VALUES 
('CCTV'), ('Valet Service'), ('Covered Parking'), ('Security Guard'), ('EV Charging');

CREATE TABLE parking_facilities_mapping (
    parking_id BIGINT NOT NULL,
    facility_id INT NOT NULL,
    PRIMARY KEY (parking_id, facility_id),
    CONSTRAINT fk_pfm_parking FOREIGN KEY (parking_id) REFERENCES parking_listings(id),
    CONSTRAINT fk_pfm_facility FOREIGN KEY (facility_id) REFERENCES facilities(id)
);


CREATE TABLE parking_vehicle_configs (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    parking_id BIGINT NOT NULL,
    vehicle_type_id INT NOT NULL,
    
    -- Capacity
    max_capacity INT NOT NULL DEFAULT 0,
    
    -- Pricing Plans
    hourly_rate DECIMAL(10, 2) DEFAULT 0.00,
    daily_rate DECIMAL(10, 2) DEFAULT 0.00,
    weekly_rate DECIMAL(10, 2) DEFAULT 0.00,
    monthly_rate DECIMAL(10, 2) DEFAULT 0.00,
    
    CONSTRAINT unique_parking_vehicle 
        UNIQUE (parking_id, vehicle_type_id),
    
    CONSTRAINT fk_pvc_parking 
        FOREIGN KEY (parking_id) 
        REFERENCES parking_listings(id),
        
    CONSTRAINT fk_pvc_vehicle_type 
        FOREIGN KEY (vehicle_type_id) 
        REFERENCES vehicle_types(id)
);




