CREATE TABLE reports (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	reporter_id bigint NOT NULL REFERENCES users(id),
	recipe_id bigint NOT NULL REFERENCES recipes(id),
	content text NOT NULL,
	created_at timestamptz NOT NULL
);
