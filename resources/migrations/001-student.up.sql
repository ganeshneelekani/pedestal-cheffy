CREATE TABLE IF NOT EXISTS students (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  date_of_birth VARCHAR(255) NOT NULL,
  enrollment_date VARCHAR(255) NOT NULL,
  graduation_date VARCHAR(255),
  major VARCHAR(255)
);
--;;