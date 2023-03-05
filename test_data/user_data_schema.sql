--
-- PostgreSQL database dump
--

-- Dumped from database version 11.8
-- Dumped by pg_dump version 13.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET schema 'public';


CREATE TABLE if not exists user_details (
    FirstName character varying(500),
    LastName character varying(500),
    Gender character varying(10),
    Country character varying(100),
    Age integer,
    SDate character varying(100),
    Id integer
);


--;;
create table if not exists account (
    uid text not null primary key,
    "name" text,
    picture text,
    unique(uid)
);
--;;
create table if not exists recipe (
    recipe_id text not null primary key,
    "public" boolean not null,
    prep_time int not null,
    "name" text not null,
    img text,
    favorite_count int check (favorite_count >= 0) default 0,
    uid text not null references account(uid) on delete cascade
);
--;;

create table if not exists step (
    step_id text not null primary key,
    sort int not null,
    description text not null,
    recipe_id text not null references recipe(recipe_id) on delete cascade
);

--;;

create table if not exists ingredient (
    ingredient_id text not null primary key,
    sort int not null,
    "name" text not null,
    amount int not null,
    measure text not null,
    recipe_id text not null references recipe(recipe_id) on delete cascade
);

--;;

create table if not exists conversation (
    conversation_id text not null,
    uid text not null,
    notifications int not null check (notifications >= 0) default 0,
    primary key (conversation_id, uid)
);

--;;

create table if not exists message (
    message_id text not null primary key,
    message_body text not null,
    uid text not null references account(uid) on delete cascade,
    conversation_id text not null,
    created_at timestamp not null default now()
);

--;;

create table if not exists recipe_favorite (
    id serial not null primary key,
    recipe_id text not null references recipe(recipe_id) on delete cascade,
    uid text not null references account(uid) on delete cascade
);


--;;

ALTER TABLE user_details OWNER TO operational;
ALTER TABLE account OWNER TO operational;
ALTER TABLE recipe OWNER TO operational;
ALTER TABLE step OWNER TO operational;
ALTER TABLE ingredient OWNER TO operational;
ALTER TABLE conversation OWNER TO operational;
ALTER TABLE message OWNER TO operational;
ALTER TABLE recipe_favorite OWNER TO operational;

--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: user
--

GRANT ALL ON SCHEMA public TO operational;


--
-- PostgreSQL database dump complete
--

