-- Indexes matching the access patterns in REQUIREMENTS.md:
-- directory filters (F1) and insight group-bys / outlier detection (F3, F5).
CREATE INDEX idx_employee_country       ON employee (country_code);
CREATE INDEX idx_employee_department    ON employee (department);
CREATE INDEX idx_employee_title_country ON employee (job_title, country_code);

-- Requirements specify a positive salary; V1 allowed zero.
ALTER TABLE employee DROP CONSTRAINT employee_annual_salary_check;
ALTER TABLE employee ADD CONSTRAINT employee_annual_salary_positive CHECK (annual_salary > 0);
