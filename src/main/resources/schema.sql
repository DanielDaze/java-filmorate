CREATE TABLE IF NOT EXISTS film (
    film_id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    duration integer,
    release_date date,
    description varchar,
    name varchar NOT NULL,
    rating_id integer DEFAULT 6
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id integer,
    genre_id integer
);

CREATE TABLE IF NOT EXISTS likes (
    film_id integer,
    user_id integer
);

CREATE TABLE IF NOT EXISTS rating (
    rating_id integer,
    name varchar
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id integer,
    name varchar
);

CREATE TABLE IF NOT EXISTS users (
    user_id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    birthday date,
    name varchar,
    login varchar,
    email varchar
);

CREATE TABLE IF NOT EXISTS friend (
    user_id integer,
    second_user_id integer,
    confirmed boolean
);