CREATE TABLE recipes (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	author_id bigint NOT NULL REFERENCES users(id),
	menu_id bigint NOT NULL REFERENCES menus(id),
	title text NOT NULL,
	created_at timestamptz NOT NULL
);

CREATE TABLE recipe_options (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	recipe_id bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
	name text NOT NULL,
	position integer NOT NULL,
	created_at timestamptz NOT NULL
);
