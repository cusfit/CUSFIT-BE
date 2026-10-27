CREATE TABLE users (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	nickname text NOT NULL,
	created_at timestamptz NOT NULL
);
