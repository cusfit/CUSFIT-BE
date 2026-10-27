CREATE TABLE brands (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	name text NOT NULL,
	created_at timestamptz NOT NULL
);

CREATE TABLE menus (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	brand_id bigint NOT NULL REFERENCES brands(id),
	name text NOT NULL,
	created_at timestamptz NOT NULL
);

CREATE TABLE option_presets (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	brand_id bigint NOT NULL REFERENCES brands(id),
	name text NOT NULL,
	created_at timestamptz NOT NULL
);
