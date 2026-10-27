CREATE TABLE terms (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	created_at timestamptz NOT NULL,
	type text NOT NULL,
	version text NOT NULL,
	content_url text NOT NULL,
	effective_at timestamptz NOT NULL
);
