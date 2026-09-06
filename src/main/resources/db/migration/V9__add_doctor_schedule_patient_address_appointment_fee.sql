-- Adds fields needed for a fully-featured demo dataset (see
-- com.healthcareai.seed.DemoDataSeeder):
--   * doctors gain a weekly working schedule
--   * patients gain a postal address
--   * appointments gain a consultation fee, so revenue can be computed
--     from completed appointments instead of being hard-coded.
ALTER TABLE doctors
    ADD COLUMN working_hours_start TIME        NOT NULL DEFAULT '09:00',
    ADD COLUMN working_hours_end   TIME        NOT NULL DEFAULT '17:00',
    ADD COLUMN working_days        VARCHAR(50) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI';

ALTER TABLE patients
    ADD COLUMN address VARCHAR(255);

ALTER TABLE appointments
    ADD COLUMN consultation_fee NUMERIC(10, 2);
