CREATE TABLE notices (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	title text NOT NULL,
	content text NOT NULL,
	created_at timestamptz NOT NULL
);

CREATE TABLE faqs (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	question text NOT NULL,
	answer text NOT NULL,
	created_at timestamptz NOT NULL
);

CREATE TABLE inquiries (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	author_id bigint NOT NULL REFERENCES users(id),
	title text NOT NULL,
	content text NOT NULL,
	created_at timestamptz NOT NULL
);
