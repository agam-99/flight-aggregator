-- Comprehensive Test Data for Airline Aggregator System
-- This file contains extensive test data including hundreds of flight routines

-- Clear existing data
TRUNCATE TABLE payments, bookings, flight_routines, route_segments, flights, airports, airlines, users RESTART IDENTITY CASCADE;

-- Insert Airlines (15 major Indian airlines)
INSERT INTO airlines (code, name, logo_url, api_config, is_active) VALUES
('AI', 'Air India', 'https://logos.textgiraffe.com/logos/logo-name/AirIndia-designstyle-wings-m.png', '{"endpoint": "https://api.airindia.in", "timeout": 30}', true),
('6E', 'IndiGo', 'https://logos.textgiraffe.com/logos/logo-name/Indigo-designstyle-wings-m.png', '{"endpoint": "https://api.goindigo.in", "timeout": 25}', true),
('SG', 'SpiceJet', 'https://logos.textgiraffe.com/logos/logo-name/SpiceJet-designstyle-wings-m.png', '{"endpoint": "https://api.spicejet.com", "timeout": 20}', true),
('UK', 'Vistara', 'https://logos.textgiraffe.com/logos/logo-name/Vistara-designstyle-wings-m.png', '{"endpoint": "https://api.airvistara.com", "timeout": 30}', true),
('G8', 'GoAir', 'https://logos.textgiraffe.com/logos/logo-name/GoAir-designstyle-wings-m.png', '{"endpoint": "https://api.goair.in", "timeout": 25}', true),
('I5', 'AirAsia India', 'https://logos.textgiraffe.com/logos/logo-name/AirAsia-designstyle-wings-m.png', '{"endpoint": "https://api.airasia.com", "timeout": 20}', true),
('9W', 'Jet Airways', 'https://logos.textgiraffe.com/logos/logo-name/JetAirways-designstyle-wings-m.png', '{"endpoint": "https://api.jetairways.com", "timeout": 30}', false),
('IX', 'Air India Express', 'https://logos.textgiraffe.com/logos/logo-name/AirIndiaExpress-designstyle-wings-m.png', '{"endpoint": "https://api.airindiaexpress.in", "timeout": 25}', true),
('QP', 'Akasa Air', 'https://logos.textgiraffe.com/logos/logo-name/AkasaAir-designstyle-wings-m.png', '{"endpoint": "https://api.akasaair.com", "timeout": 20}', true),
('S2', 'JetLite', 'https://logos.textgiraffe.com/logos/logo-name/JetLite-designstyle-wings-m.png', '{"endpoint": "https://api.jetlite.com", "timeout": 25}', false),
('DN', 'Regional Air', 'https://logos.textgiraffe.com/logos/logo-name/RegionalAir-designstyle-wings-m.png', '{"endpoint": "https://api.regionalair.in", "timeout": 20}', true),
('QG', 'Air Costa', 'https://logos.textgiraffe.com/logos/logo-name/AirCosta-designstyle-wings-m.png', '{"endpoint": "https://api.aircosta.in", "timeout": 25}', false),
('LB', 'Air Pegasus', 'https://logos.textgiraffe.com/logos/logo-name/AirPegasus-designstyle-wings-m.png', '{"endpoint": "https://api.airpegasus.in", "timeout": 20}', false),
('S9', 'Air Carnival', 'https://logos.textgiraffe.com/logos/logo-name/AirCarnival-designstyle-wings-m.png', '{"endpoint": "https://api.aircarnival.in", "timeout": 25}', false),
('VL', 'Air Mantra', 'https://logos.textgiraffe.com/logos/logo-name/AirMantra-designstyle-wings-m.png', '{"endpoint": "https://api.airmantra.in", "timeout": 20}', true);

-- Insert Airports (30 major Indian airports)
INSERT INTO airports (code, name, city, country, timezone, latitude, longitude, is_active) VALUES
('DEL', 'Indira Gandhi International Airport', 'Delhi', 'India', 'Asia/Kolkata', 28.5665, 77.1031, true),
('BOM', 'Chhatrapati Shivaji Maharaj International Airport', 'Mumbai', 'India', 'Asia/Kolkata', 19.0896, 72.8656, true),
('BLR', 'Kempegowda International Airport', 'Bangalore', 'India', 'Asia/Kolkata', 13.1986, 77.7066, true),
('MAA', 'Chennai International Airport', 'Chennai', 'India', 'Asia/Kolkata', 12.9941, 80.1709, true),
('HYD', 'Rajiv Gandhi International Airport', 'Hyderabad', 'India', 'Asia/Kolkata', 17.2403, 78.4294, true),
('CCU', 'Netaji Subhas Chandra Bose International Airport', 'Kolkata', 'India', 'Asia/Kolkata', 22.6547, 88.4467, true),
('AMD', 'Sardar Vallabhbhai Patel International Airport', 'Ahmedabad', 'India', 'Asia/Kolkata', 23.0832, 72.6347, true),
('PNQ', 'Pune International Airport', 'Pune', 'India', 'Asia/Kolkata', 18.5793, 73.9089, true),
('COK', 'Cochin International Airport', 'Kochi', 'India', 'Asia/Kolkata', 10.1520, 76.4019, true),
('TRV', 'Thiruvananthapuram International Airport', 'Thiruvananthapuram', 'India', 'Asia/Kolkata', 8.4821, 76.9200, true),
('GOI', 'Goa International Airport', 'Goa', 'India', 'Asia/Kolkata', 15.3808, 73.8314, true),
('JAI', 'Jaipur International Airport', 'Jaipur', 'India', 'Asia/Kolkata', 26.8821, 75.8100, true),
('LKO', 'Chaudhary Charan Singh International Airport', 'Lucknow', 'India', 'Asia/Kolkata', 26.7606, 80.8893, true),
('CJB', 'Coimbatore International Airport', 'Coimbatore', 'India', 'Asia/Kolkata', 11.0297, 77.0436, true),
('TRZ', 'Tiruchirapalli International Airport', 'Trichy', 'India', 'Asia/Kolkata', 10.7650, 78.7097, true),
('VNS', 'Lal Bahadur Shastri Airport', 'Varanasi', 'India', 'Asia/Kolkata', 25.4522, 82.8592, true),
('IXC', 'Chandigarh International Airport', 'Chandigarh', 'India', 'Asia/Kolkata', 30.6735, 76.7884, true),
('GAU', 'Lokpriya Gopinath Bordoloi International Airport', 'Guwahati', 'India', 'Asia/Kolkata', 26.1061, 91.5859, true),
('BBI', 'Biju Patnaik International Airport', 'Bhubaneswar', 'India', 'Asia/Kolkata', 20.2441, 85.8178, true),
('IXM', 'Madurai International Airport', 'Madurai', 'India', 'Asia/Kolkata', 9.8349, 78.0936, true),
('VGA', 'Vijayanagara Airport', 'Ballari', 'India', 'Asia/Kolkata', 15.1649, 76.8841, true),
('IXZ', 'Veer Savarkar International Airport', 'Port Blair', 'India', 'Asia/Kolkata', 11.6410, 92.7296, true),
('IXR', 'Birsa Munda Airport', 'Ranchi', 'India', 'Asia/Kolkata', 23.3144, 85.3217, true),
('NAG', 'Dr. Babasaheb Ambedkar International Airport', 'Nagpur', 'India', 'Asia/Kolkata', 21.0924, 79.0473, true),
('RPR', 'Swami Vivekananda Airport', 'Raipur', 'India', 'Asia/Kolkata', 21.1804, 81.7387, true),
('IXI', 'Lilabari Airport', 'North Lakhimpur', 'India', 'Asia/Kolkata', 27.2955, 94.0977, true),
('IXS', 'Silchar Airport', 'Silchar', 'India', 'Asia/Kolkata', 24.9129, 92.9787, true),
('AGR', 'Agra Airport', 'Agra', 'India', 'Asia/Kolkata', 27.1577, 77.9608, true),
('IXA', 'Agartala Airport', 'Agartala', 'India', 'Asia/Kolkata', 23.8870, 91.2403, true),
('IXL', 'Leh Kushok Bakula Rimpochee Airport', 'Leh', 'India', 'Asia/Kolkata', 34.1358, 77.5465, true);

-- Insert Users (50 sample users)
INSERT INTO users (email, password_hash, first_name, last_name, phone, preferences) VALUES
('john.doe@example.com', '$2a$10$example_hash1', 'John', 'Doe', '+91-9876543210', '{"seatPreference": "window", "mealPreference": "veg"}'),
('jane.smith@example.com', '$2a$10$example_hash2', 'Jane', 'Smith', '+91-9876543211', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('rahul.kumar@example.com', '$2a$10$example_hash3', 'Rahul', 'Kumar', '+91-9876543212', '{"seatPreference": "window", "mealPreference": "veg"}'),
('priya.sharma@example.com', '$2a$10$example_hash4', 'Priya', 'Sharma', '+91-9876543213', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('amit.patel@example.com', '$2a$10$example_hash5', 'Amit', 'Patel', '+91-9876543214', '{"seatPreference": "middle", "mealPreference": "non-veg"}'),
('neha.gupta@example.com', '$2a$10$example_hash6', 'Neha', 'Gupta', '+91-9876543215', '{"seatPreference": "window", "mealPreference": "veg"}'),
('vikash.singh@example.com', '$2a$10$example_hash7', 'Vikash', 'Singh', '+91-9876543216', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('swati.rao@example.com', '$2a$10$example_hash8', 'Swati', 'Rao', '+91-9876543217', '{"seatPreference": "window", "mealPreference": "veg"}'),
('ravi.krishna@example.com', '$2a$10$example_hash9', 'Ravi', 'Krishna', '+91-9876543218', '{"seatPreference": "aisle", "mealPreference": "jain"}'),
('anita.desai@example.com', '$2a$10$example_hash10', 'Anita', 'Desai', '+91-9876543219', '{"seatPreference": "window", "mealPreference": "veg"}'),
('suresh.reddy@example.com', '$2a$10$example_hash11', 'Suresh', 'Reddy', '+91-9876543220', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('kavita.nair@example.com', '$2a$10$example_hash12', 'Kavita', 'Nair', '+91-9876543221', '{"seatPreference": "window", "mealPreference": "veg"}'),
('deepak.joshi@example.com', '$2a$10$example_hash13', 'Deepak', 'Joshi', '+91-9876543222', '{"seatPreference": "middle", "mealPreference": "veg"}'),
('meera.iyer@example.com', '$2a$10$example_hash14', 'Meera', 'Iyer', '+91-9876543223', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('arun.malhotra@example.com', '$2a$10$example_hash15', 'Arun', 'Malhotra', '+91-9876543224', '{"seatPreference": "window", "mealPreference": "non-veg"}'),
('pooja.aggarwal@example.com', '$2a$10$example_hash16', 'Pooja', 'Aggarwal', '+91-9876543225', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('manish.verma@example.com', '$2a$10$example_hash17', 'Manish', 'Verma', '+91-9876543226', '{"seatPreference": "window", "mealPreference": "non-veg"}'),
('sunita.pandey@example.com', '$2a$10$example_hash18', 'Sunita', 'Pandey', '+91-9876543227', '{"seatPreference": "middle", "mealPreference": "veg"}'),
('raj.kapoor@example.com', '$2a$10$example_hash19', 'Raj', 'Kapoor', '+91-9876543228', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('shilpa.mehta@example.com', '$2a$10$example_hash20', 'Shilpa', 'Mehta', '+91-9876543229', '{"seatPreference": "window", "mealPreference": "jain"}'),
('rohit.saxena@example.com', '$2a$10$example_hash21', 'Rohit', 'Saxena', '+91-9876543230', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('asha.bhatt@example.com', '$2a$10$example_hash22', 'Asha', 'Bhatt', '+91-9876543231', '{"seatPreference": "window", "mealPreference": "veg"}'),
('nitin.goel@example.com', '$2a$10$example_hash23', 'Nitin', 'Goel', '+91-9876543232', '{"seatPreference": "middle", "mealPreference": "non-veg"}'),
('rekha.tiwari@example.com', '$2a$10$example_hash24', 'Rekha', 'Tiwari', '+91-9876543233', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('vinod.chouhan@example.com', '$2a$10$example_hash25', 'Vinod', 'Chouhan', '+91-9876543234', '{"seatPreference": "window", "mealPreference": "non-veg"}'),
('madhuri.dave@example.com', '$2a$10$example_hash26', 'Madhuri', 'Dave', '+91-9876543235', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('prakash.jain@example.com', '$2a$10$example_hash27', 'Prakash', 'Jain', '+91-9876543236', '{"seatPreference": "window", "mealPreference": "jain"}'),
('sangita.roy@example.com', '$2a$10$example_hash28', 'Sangita', 'Roy', '+91-9876543237', '{"seatPreference": "middle", "mealPreference": "veg"}'),
('ajay.sinha@example.com', '$2a$10$example_hash29', 'Ajay', 'Sinha', '+91-9876543238', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('nirmala.shah@example.com', '$2a$10$example_hash30', 'Nirmala', 'Shah', '+91-9876543239', '{"seatPreference": "window", "mealPreference": "veg"}'),
('sanjay.mishra@example.com', '$2a$10$example_hash31', 'Sanjay', 'Mishra', '+91-9876543240', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('uma.kulkarni@example.com', '$2a$10$example_hash32', 'Uma', 'Kulkarni', '+91-9876543241', '{"seatPreference": "window", "mealPreference": "veg"}'),
('dinesh.bhagat@example.com', '$2a$10$example_hash33', 'Dinesh', 'Bhagat', '+91-9876543242', '{"seatPreference": "middle", "mealPreference": "non-veg"}'),
('kavya.pillai@example.com', '$2a$10$example_hash34', 'Kavya', 'Pillai', '+91-9876543243', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('manoj.thakur@example.com', '$2a$10$example_hash35', 'Manoj', 'Thakur', '+91-9876543244', '{"seatPreference": "window", "mealPreference": "non-veg"}'),
('lata.bansal@example.com', '$2a$10$example_hash36', 'Lata', 'Bansal', '+91-9876543245', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('ramesh.agarwal@example.com', '$2a$10$example_hash37', 'Ramesh', 'Agarwal', '+91-9876543246', '{"seatPreference": "window", "mealPreference": "jain"}'),
('geeta.bose@example.com', '$2a$10$example_hash38', 'Geeta', 'Bose', '+91-9876543247', '{"seatPreference": "middle", "mealPreference": "veg"}'),
('harish.sethi@example.com', '$2a$10$example_hash39', 'Harish', 'Sethi', '+91-9876543248', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('namita.ghosh@example.com', '$2a$10$example_hash40', 'Namita', 'Ghosh', '+91-9876543249', '{"seatPreference": "window", "mealPreference": "veg"}'),
('kishore.yadav@example.com', '$2a$10$example_hash41', 'Kishore', 'Yadav', '+91-9876543250', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('beena.soni@example.com', '$2a$10$example_hash42', 'Beena', 'Soni', '+91-9876543251', '{"seatPreference": "window", "mealPreference": "veg"}'),
('naveen.chand@example.com', '$2a$10$example_hash43', 'Naveen', 'Chand', '+91-9876543252', '{"seatPreference": "middle", "mealPreference": "non-veg"}'),
('sarla.dutta@example.com', '$2a$10$example_hash44', 'Sarla', 'Dutta', '+91-9876543253', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('mukesh.rastogi@example.com', '$2a$10$example_hash45', 'Mukesh', 'Rastogi', '+91-9876543254', '{"seatPreference": "window", "mealPreference": "non-veg"}'),
('shanti.bhardwaj@example.com', '$2a$10$example_hash46', 'Shanti', 'Bhardwaj', '+91-9876543255', '{"seatPreference": "aisle", "mealPreference": "veg"}'),
('yogesh.bajaj@example.com', '$2a$10$example_hash47', 'Yogesh', 'Bajaj', '+91-9876543256', '{"seatPreference": "window", "mealPreference": "jain"}'),
('anjali.saxena@example.com', '$2a$10$example_hash48', 'Anjali', 'Saxena', '+91-9876543257', '{"seatPreference": "middle", "mealPreference": "veg"}'),
('ashok.malkan@example.com', '$2a$10$example_hash49', 'Ashok', 'Malkan', '+91-9876543258', '{"seatPreference": "aisle", "mealPreference": "non-veg"}'),
('nisha.tandon@example.com', '$2a$10$example_hash50', 'Nisha', 'Tandon', '+91-9876543259', '{"seatPreference": "window", "mealPreference": "veg"}');

-- Create a simpler approach to generate flights and flight routines
-- First, let's create popular direct routes
INSERT INTO flights (airline_id, flight_number, source_airport, destination_airport, route_display, metadata, total_duration_minutes, is_active)
SELECT 
  a.id as airline_id,
  a.code || '-' || LPAD((100 + (ROW_NUMBER() OVER ()) % 899)::text, 3, '0') as flight_number,
  routes.source_airport,
  routes.destination_airport,
  routes.source_airport || ' -> ' || routes.destination_airport as route_display,
  ('{"aircraft_type": "' || 
    CASE (ROW_NUMBER() OVER ()) % 4
      WHEN 0 THEN 'Airbus A320neo'
      WHEN 1 THEN 'Boeing 737-800'
      WHEN 2 THEN 'Airbus A321'
      ELSE 'Boeing 737 MAX'
    END || 
    '", "amenities": ["WiFi", "Entertainment"], "cabin_config": "3-3"}')::jsonb as metadata,
  routes.duration_minutes,
  true as is_active
FROM (
  -- Popular direct routes with realistic durations
  SELECT 'DEL' as source_airport, 'BOM' as destination_airport, 135 as duration_minutes
  UNION ALL SELECT 'DEL', 'BLR', 180
  UNION ALL SELECT 'DEL', 'MAA', 165
  UNION ALL SELECT 'DEL', 'CCU', 145
  UNION ALL SELECT 'DEL', 'AMD', 90
  UNION ALL SELECT 'DEL', 'HYD', 150
  UNION ALL SELECT 'DEL', 'COK', 195
  UNION ALL SELECT 'DEL', 'GOI', 120
  UNION ALL SELECT 'DEL', 'JAI', 45
  UNION ALL SELECT 'DEL', 'LKO', 60
  
  UNION ALL SELECT 'BOM', 'DEL', 135
  UNION ALL SELECT 'BOM', 'BLR', 105
  UNION ALL SELECT 'BOM', 'MAA', 120
  UNION ALL SELECT 'BOM', 'CCU', 135
  UNION ALL SELECT 'BOM', 'HYD', 75
  UNION ALL SELECT 'BOM', 'AMD', 60
  UNION ALL SELECT 'BOM', 'GOI', 45
  UNION ALL SELECT 'BOM', 'PNQ', 30
  UNION ALL SELECT 'BOM', 'COK', 90
  UNION ALL SELECT 'BOM', 'TRV', 105
  
  UNION ALL SELECT 'BLR', 'DEL', 180
  UNION ALL SELECT 'BLR', 'BOM', 105
  UNION ALL SELECT 'BLR', 'MAA', 60
  UNION ALL SELECT 'BLR', 'CCU', 135
  UNION ALL SELECT 'BLR', 'HYD', 60
  UNION ALL SELECT 'BLR', 'COK', 75
  UNION ALL SELECT 'BLR', 'AMD', 90
  UNION ALL SELECT 'BLR', 'GOI', 75
  UNION ALL SELECT 'BLR', 'CJB', 45
  UNION ALL SELECT 'BLR', 'TRZ', 60
  
  UNION ALL SELECT 'MAA', 'DEL', 165
  UNION ALL SELECT 'MAA', 'BOM', 120
  UNION ALL SELECT 'MAA', 'BLR', 60
  UNION ALL SELECT 'MAA', 'CCU', 120
  UNION ALL SELECT 'MAA', 'HYD', 75
  UNION ALL SELECT 'MAA', 'COK', 75
  UNION ALL SELECT 'MAA', 'TRV', 60
  UNION ALL SELECT 'MAA', 'CJB', 45
  UNION ALL SELECT 'MAA', 'TRZ', 30
  UNION ALL SELECT 'MAA', 'IXM', 45
  
  UNION ALL SELECT 'HYD', 'DEL', 150
  UNION ALL SELECT 'HYD', 'BOM', 75
  UNION ALL SELECT 'HYD', 'BLR', 60
  UNION ALL SELECT 'HYD', 'MAA', 75
  UNION ALL SELECT 'HYD', 'CCU', 105
  UNION ALL SELECT 'HYD', 'AMD', 90
  UNION ALL SELECT 'HYD', 'VNS', 90
  UNION ALL SELECT 'HYD', 'NAG', 60
  
  UNION ALL SELECT 'CCU', 'DEL', 145
  UNION ALL SELECT 'CCU', 'BOM', 135
  UNION ALL SELECT 'CCU', 'BLR', 135
  UNION ALL SELECT 'CCU', 'MAA', 120
  UNION ALL SELECT 'CCU', 'HYD', 105
  UNION ALL SELECT 'CCU', 'GAU', 60
  UNION ALL SELECT 'CCU', 'BBI', 45
  UNION ALL SELECT 'CCU', 'IXR', 45
  
  -- Add more routes for tier-2 cities
  UNION ALL SELECT 'JAI', 'BOM', 90
  UNION ALL SELECT 'JAI', 'BLR', 135
  UNION ALL SELECT 'LKO', 'BOM', 105
  UNION ALL SELECT 'LKO', 'BLR', 135
  UNION ALL SELECT 'AMD', 'BOM', 60
  UNION ALL SELECT 'AMD', 'BLR', 90
  UNION ALL SELECT 'PNQ', 'DEL', 120
  UNION ALL SELECT 'PNQ', 'BLR', 75
  UNION ALL SELECT 'GOI', 'DEL', 120
  UNION ALL SELECT 'GOI', 'BLR', 75
  UNION ALL SELECT 'COK', 'DEL', 195
  UNION ALL SELECT 'COK', 'BOM', 90
  UNION ALL SELECT 'NAG', 'DEL', 90
  UNION ALL SELECT 'NAG', 'BOM', 75
) routes
CROSS JOIN airlines a
WHERE a.is_active = true;

-- Generate flight routines for the next 30 days
-- Create extensive flight schedules with 20-30 flights per day for popular routes
DO $$
DECLARE
    flight_record RECORD;
    date_offset INTEGER;
    time_slot TIME;
    -- Extended time slots from early morning to late night (30 slots)
    time_slots TIME[] := ARRAY[
        '05:00:00', '05:30:00', '06:00:00', '06:30:00', '07:00:00', '07:30:00', 
        '08:00:00', '08:30:00', '09:00:00', '09:30:00', '10:00:00', '10:30:00',
        '11:00:00', '11:30:00', '12:00:00', '12:30:00', '13:00:00', '13:30:00',
        '14:00:00', '14:30:00', '15:00:00', '15:30:00', '16:00:00', '16:30:00',
        '17:00:00', '17:30:00', '18:00:00', '18:30:00', '19:00:00', '19:30:00',
        '20:00:00', '20:30:00', '21:00:00', '21:30:00', '22:00:00', '22:30:00',
        '23:00:00', '23:30:00'
    ];
    base_price INTEGER;
    current_price DECIMAL(10,2);
    available_seats INTEGER;
    total_seats INTEGER;
    time_multiplier DECIMAL(3,2);
    weekend_multiplier DECIMAL(3,2);
    advance_multiplier DECIMAL(3,2);
    demand_multiplier DECIMAL(3,2);
    travel_date DATE;
    arrival_time TIME;
    daily_flights INTEGER;
    route_key TEXT;
    airlines_on_route INTEGER;
    slot_index INTEGER;
BEGIN
    -- Group flights by route to distribute across airlines
    FOR route_key IN 
        SELECT DISTINCT (source_airport || '-' || destination_airport) 
        FROM flights 
        WHERE is_active = true 
    LOOP
        -- Count airlines operating on this route
        SELECT COUNT(*) INTO airlines_on_route 
        FROM flights 
        WHERE is_active = true 
        AND (source_airport || '-' || destination_airport) = route_key;
        
        -- Determine flights per day based on route popularity
        IF route_key IN ('DEL-BOM', 'BOM-DEL', 'DEL-BLR', 'BLR-DEL', 'DEL-MAA', 'MAA-DEL', 
                        'BOM-BLR', 'BLR-BOM', 'BOM-MAA', 'MAA-BOM', 'BLR-MAA', 'MAA-BLR') THEN
            daily_flights := 28; -- 28 flights per day for ultra-popular routes
        ELSIF route_key LIKE 'DEL-%' OR route_key LIKE '%-DEL' OR 
              route_key LIKE 'BOM-%' OR route_key LIKE '%-BOM' OR 
              route_key LIKE 'BLR-%' OR route_key LIKE '%-BLR' OR
              route_key LIKE 'MAA-%' OR route_key LIKE '%-MAA' THEN
            daily_flights := 20; -- 20 flights per day for major city routes
        ELSIF route_key LIKE 'HYD-%' OR route_key LIKE '%-HYD' OR 
              route_key LIKE 'CCU-%' OR route_key LIKE '%-CCU' THEN
            daily_flights := 12; -- 12 flights per day for tier-1 city routes
        ELSIF route_key LIKE 'AMD-%' OR route_key LIKE '%-AMD' OR 
              route_key LIKE 'PNQ-%' OR route_key LIKE '%-PNQ' OR
              route_key LIKE 'COK-%' OR route_key LIKE '%-COK' THEN
            daily_flights := 8;  -- 8 flights per day for tier-2 city routes
        ELSE
            daily_flights := 4;  -- 4 flights per day for other routes
        END IF;
        
        slot_index := 1;
        
        -- Loop through each flight on this route
        FOR flight_record IN 
            SELECT * FROM flights 
            WHERE is_active = true 
            AND (source_airport || '-' || destination_airport) = route_key
            ORDER BY airline_id
        LOOP
            -- Calculate flights per airline (distribute evenly)
            DECLARE
                flights_per_airline INTEGER := GREATEST(1, daily_flights / airlines_on_route);
                extra_flights INTEGER := daily_flights % airlines_on_route;
                airline_flights INTEGER;
            BEGIN
                -- Give extra flights to first airlines in alphabetical order
                IF slot_index <= extra_flights THEN
                    airline_flights := flights_per_airline + 1;
                ELSE
                    airline_flights := flights_per_airline;
                END IF;
                
                -- Loop through next 30 days
                FOR date_offset IN 0..29 LOOP
                    travel_date := CURRENT_DATE + date_offset;
                    
                    -- Generate flights for this airline on this day
                    FOR i IN 1..airline_flights LOOP
                        -- Use different time slots for each airline to spread flights
                        DECLARE
                            time_slot_index INTEGER := ((slot_index - 1) * airline_flights + i - 1) % array_length(time_slots, 1) + 1;
                        BEGIN
                            time_slot := time_slots[time_slot_index];
                            arrival_time := time_slot + (flight_record.total_duration_minutes || ' minutes')::interval;
                            
                            -- Adjust arrival time if it goes to next day
                            IF arrival_time < time_slot THEN
                                arrival_time := arrival_time + INTERVAL '1 day';
                            END IF;
                            
                            -- Calculate pricing based on route popularity and airline
                            -- Base price based on route and airline positioning
                            CASE 
                                WHEN route_key IN ('DEL-BOM', 'BOM-DEL') THEN
                                    base_price := 4200 + FLOOR(RANDOM() * 800)::INTEGER; -- 4200-5000
                                WHEN route_key IN ('DEL-BLR', 'BLR-DEL') THEN
                                    base_price := 3800 + FLOOR(RANDOM() * 700)::INTEGER; -- 3800-4500
                                WHEN route_key IN ('DEL-MAA', 'MAA-DEL') THEN
                                    base_price := 5200 + FLOOR(RANDOM() * 1000)::INTEGER; -- 5200-6200
                                WHEN route_key IN ('BOM-BLR', 'BLR-BOM') THEN
                                    base_price := 3500 + FLOOR(RANDOM() * 600)::INTEGER; -- 3500-4100
                                WHEN route_key IN ('BOM-MAA', 'MAA-BOM', 'BLR-MAA', 'MAA-BLR') THEN
                                    base_price := 3200 + FLOOR(RANDOM() * 500)::INTEGER; -- 3200-3700
                                WHEN route_key LIKE 'DEL-%' OR route_key LIKE '%-DEL' THEN
                                    base_price := 3000 + FLOOR(RANDOM() * 800)::INTEGER; -- 3000-3800
                                WHEN route_key LIKE 'BOM-%' OR route_key LIKE '%-BOM' THEN
                                    base_price := 2800 + FLOOR(RANDOM() * 700)::INTEGER; -- 2800-3500
                                WHEN route_key LIKE 'BLR-%' OR route_key LIKE '%-BLR' THEN
                                    base_price := 2600 + FLOOR(RANDOM() * 600)::INTEGER; -- 2600-3200
                                ELSE
                                    base_price := 2200 + FLOOR(RANDOM() * 800)::INTEGER; -- 2200-3000
                            END CASE;
                            
                            -- Airline premium adjustments
                            IF flight_record.flight_number LIKE 'AI-%' THEN
                                base_price := base_price * 1.15; -- Air India premium
                            ELSIF flight_record.flight_number LIKE 'UK-%' THEN
                                base_price := base_price * 1.25; -- Vistara premium
                            ELSIF flight_record.flight_number LIKE '6E-%' THEN
                                base_price := base_price * 0.95; -- IndiGo competitive
                            ELSIF flight_record.flight_number LIKE 'SG-%' THEN
                                base_price := base_price * 0.90; -- SpiceJet budget
                            ELSIF flight_record.flight_number LIKE 'G8-%' THEN
                                base_price := base_price * 0.85; -- GoAir budget
                            END IF;
                            
                            -- Time-based pricing
                            IF EXTRACT(HOUR FROM time_slot) BETWEEN 6 AND 9 OR EXTRACT(HOUR FROM time_slot) BETWEEN 17 AND 21 THEN
                                time_multiplier := 1.20; -- Peak hours
                            ELSIF EXTRACT(HOUR FROM time_slot) BETWEEN 22 AND 5 THEN
                                time_multiplier := 0.80; -- Red-eye flights
                            ELSE
                                time_multiplier := 1.0; -- Normal hours
                            END IF;
                            
                            -- Weekend multiplier
                            IF EXTRACT(DOW FROM travel_date) IN (0, 6) THEN -- Sunday = 0, Saturday = 6
                                weekend_multiplier := 1.25;
                            ELSIF EXTRACT(DOW FROM travel_date) = 5 THEN -- Friday
                                weekend_multiplier := 1.15;
                            ELSE
                                weekend_multiplier := 1.0;
                            END IF;
                            
                            -- Advance booking multiplier (last-minute pricing)
                            IF date_offset <= 1 THEN
                                advance_multiplier := 1.40; -- Very last minute
                            ELSIF date_offset <= 3 THEN
                                advance_multiplier := 1.25; -- Last minute
                            ELSIF date_offset <= 7 THEN
                                advance_multiplier := 1.10; -- Week advance
                            ELSIF date_offset <= 14 THEN
                                advance_multiplier := 1.00; -- Two weeks
                            ELSIF date_offset <= 21 THEN
                                advance_multiplier := 0.95; -- Three weeks
                            ELSE
                                advance_multiplier := 0.90; -- Month+ advance
                            END IF;
                            
                            -- Random demand multiplier (simulate market conditions)
                            demand_multiplier := 0.85 + (RANDOM() * 0.30); -- 0.85 to 1.15
                            
                            -- Calculate final price
                            current_price := (base_price * time_multiplier * weekend_multiplier * advance_multiplier * demand_multiplier)::DECIMAL(10,2);
                            
                            -- Aircraft-based seat configuration
                            total_seats := CASE 
                                WHEN flight_record.metadata->>'aircraft_type' LIKE '%A321%' THEN 220
                                WHEN flight_record.metadata->>'aircraft_type' LIKE '%A320neo%' THEN 186
                                WHEN flight_record.metadata->>'aircraft_type' LIKE '%A320%' THEN 180
                                WHEN flight_record.metadata->>'aircraft_type' LIKE '%737 MAX%' THEN 189
                                WHEN flight_record.metadata->>'aircraft_type' LIKE '%737-800%' THEN 162
                                ELSE 180
                            END;
                            
                            -- Realistic occupancy patterns
                            DECLARE
                                base_occupancy INTEGER;
                            BEGIN
                                -- Base occupancy depends on route popularity and time
                                IF route_key IN ('DEL-BOM', 'BOM-DEL', 'DEL-BLR', 'BLR-DEL') THEN
                                    base_occupancy := 70 + FLOOR(RANDOM() * 25)::INTEGER; -- 70-95% for popular routes
                                ELSIF EXTRACT(HOUR FROM time_slot) BETWEEN 6 AND 9 OR EXTRACT(HOUR FROM time_slot) BETWEEN 17 AND 21 THEN
                                    base_occupancy := 60 + FLOOR(RANDOM() * 30)::INTEGER; -- 60-90% for peak hours
                                ELSIF EXTRACT(HOUR FROM time_slot) BETWEEN 22 AND 5 THEN
                                    base_occupancy := 30 + FLOOR(RANDOM() * 40)::INTEGER; -- 30-70% for red-eye
                                ELSE
                                    base_occupancy := 45 + FLOOR(RANDOM() * 40)::INTEGER; -- 45-85% normal
                                END IF;
                                
                                -- Weekend adjustment
                                IF EXTRACT(DOW FROM travel_date) IN (0, 6) THEN
                                    base_occupancy := LEAST(95, base_occupancy + 10);
                                END IF;
                                
                                -- Last-minute booking surge
                                IF date_offset <= 3 THEN
                                    base_occupancy := LEAST(95, base_occupancy + 5);
                                END IF;
                                
                                available_seats := total_seats - (total_seats * base_occupancy / 100);
                                available_seats := GREATEST(0, available_seats); -- Ensure non-negative
                            END;
                            
                            -- Insert flight routine
                            INSERT INTO flight_routines (
                                flight_id, travel_date, departure_time, arrival_time, 
                                total_seats, available_seats, base_price, current_price, 
                                currency, status, pricing_tiers, price_updated_at, availability_updated_at
                            ) VALUES (
                                flight_record.id,
                                travel_date,
                                time_slot,
                                arrival_time::TIME,
                                total_seats,
                                available_seats,
                                base_price,
                                current_price,
                                'INR',
                                'scheduled',
                                ('{"economy": ' || current_price::INTEGER || 
                                 ', "business": ' || (current_price * 2.5)::INTEGER || 
                                 ', "first": ' || (current_price * 4.2)::INTEGER || '}')::jsonb,
                                NOW(),
                                NOW()
                            );
                        END;
                    END LOOP;
                END LOOP;
                
                slot_index := slot_index + 1;
            END;
        END LOOP;
    END LOOP;
END $$;

-- Insert some sample bookings
INSERT INTO bookings (
  user_id, flight_routine_id, status, pnr, total_amount, currency,
  passenger_details, contact_info, expires_at
)
SELECT 
  u.user_id,
  fr.id as flight_routine_id,
  CASE (ROW_NUMBER() OVER ()) % 4
    WHEN 0 THEN 'confirmed'
    WHEN 1 THEN 'pending'
    WHEN 2 THEN 'expired'
    ELSE 'confirmed'
  END as status,
  CASE 
    WHEN (ROW_NUMBER() OVER ()) % 4 != 1 THEN 
      CHR(65 + (RANDOM() * 25)::INTEGER) ||
      CHR(65 + (RANDOM() * 25)::INTEGER) ||
      CHR(65 + (RANDOM() * 25)::INTEGER) ||
      (100 + FLOOR(RANDOM() * 900))::TEXT
    ELSE NULL
  END as pnr,
  fr.current_price * (1 + FLOOR(RANDOM() * 3)) as total_amount,
  'INR',
  '[{"title": "Mr", "firstName": "John", "lastName": "Doe", "dateOfBirth": "1990-01-15", "nationality": "Indian", "seatPreference": "window"}]'::jsonb,
  '{"email": "test@example.com", "phone": "+91-9876543210"}'::jsonb,
  CASE 
    WHEN (ROW_NUMBER() OVER ()) % 4 = 1 THEN NOW() + INTERVAL '15 minutes'
    ELSE NOW() - INTERVAL '1 hour'
  END as expires_at
FROM users u
CROSS JOIN LATERAL (
  SELECT * FROM flight_routines fr 
  WHERE fr.travel_date >= CURRENT_DATE 
    AND fr.available_seats >= 1
  ORDER BY RANDOM() 
  LIMIT 1
) fr
WHERE u.email LIKE '%example.com'
LIMIT 30;

-- Insert some sample payments for confirmed bookings
INSERT INTO payments (
  booking_id, amount, currency, status, payment_method, 
  gateway_provider, transaction_id, gateway_response, processed_at
)
SELECT 
  b.booking_id,
  b.total_amount,
  b.currency,
  'success' as status,
  CASE FLOOR(RANDOM() * 4)::INTEGER
    WHEN 0 THEN 'card'
    WHEN 1 THEN 'upi'
    WHEN 2 THEN 'netbanking'
    ELSE 'wallet'
  END as payment_method,
  'mock_gateway' as gateway_provider,
  'TXN_' || EXTRACT(EPOCH FROM NOW())::BIGINT || '_' || FLOOR(RANDOM() * 1000)::INTEGER as transaction_id,
  '{"gateway": "mock", "status": "success", "reference": "mock_ref_123"}'::jsonb as gateway_response,
  b.created_at + INTERVAL '2 minutes' as processed_at
FROM bookings b
WHERE b.status = 'confirmed';

-- Update flight routine availability based on bookings
UPDATE flight_routines 
SET available_seats = available_seats - 1
WHERE id IN (
  SELECT DISTINCT flight_routine_id 
  FROM bookings 
  WHERE status = 'confirmed'
);

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO airline_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO airline_user;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_flight_routines_search ON flight_routines(travel_date, departure_time) WHERE status = 'scheduled';
CREATE INDEX IF NOT EXISTS idx_flight_routines_availability ON flight_routines(available_seats) WHERE available_seats > 0;
CREATE INDEX IF NOT EXISTS idx_flights_route ON flights(source_airport, destination_airport) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_expires ON bookings(expires_at) WHERE status = 'pending';

-- Print summary
DO $$
DECLARE
    airline_count INTEGER;
    airport_count INTEGER;
    flight_count INTEGER;
    routine_count INTEGER;
    user_count INTEGER;
    booking_count INTEGER;
    payment_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO airline_count FROM airlines;
    SELECT COUNT(*) INTO airport_count FROM airports;
    SELECT COUNT(*) INTO flight_count FROM flights;
    SELECT COUNT(*) INTO routine_count FROM flight_routines;
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO booking_count FROM bookings;
    SELECT COUNT(*) INTO payment_count FROM payments;
    
    RAISE NOTICE '=== COMPREHENSIVE DATA IMPORT COMPLETED ===';
    RAISE NOTICE 'Airlines: %', airline_count;
    RAISE NOTICE 'Airports: %', airport_count;
    RAISE NOTICE 'Flights: %', flight_count;
    RAISE NOTICE 'Flight Routines: %', routine_count;
    RAISE NOTICE 'Users: %', user_count;
    RAISE NOTICE 'Bookings: %', booking_count;
    RAISE NOTICE 'Payments: %', payment_count;
    RAISE NOTICE '==========================================';
END $$;