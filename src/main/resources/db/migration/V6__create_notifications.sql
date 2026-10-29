CREATE TABLE notifications (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	recipient_id bigint NOT NULL REFERENCES users(id),
	title text NOT NULL,
	content text NOT NULL,
	created_at timestamptz NOT NULL
);
