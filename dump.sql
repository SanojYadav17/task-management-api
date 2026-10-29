--
-- PostgreSQL database dump
-- Database: task_management_db
-- Assignment: Java REST API for Task Management System
-- Candidate: Sanoj Yadav
-- Company: Ikaansh Edutech Private Limited
-- Dumped at: 2026-10-29 15:45:00
--

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

--
-- Drop existing tables and sequences
--
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
DROP SEQUENCE IF EXISTS public.tasks_id_seq;
DROP SEQUENCE IF EXISTS public.users_id_seq;

SET default_tablespace = '';
SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(20) DEFAULT 'ROLE_USER'::character varying NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.users_id_seq OWNER TO postgres;
ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

--
-- Name: tasks; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE public.tasks (
    id bigint NOT NULL,
    title character varying(150) NOT NULL,
    description text,
    due_date timestamp without time zone,
    priority character varying(20) DEFAULT 'MEDIUM'::character varying NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    completed boolean DEFAULT false NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

ALTER TABLE public.tasks OWNER TO postgres;

--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--
CREATE SEQUENCE public.tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.tasks_id_seq OWNER TO postgres;
ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;
ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--
-- Password for admin: AdminPassword123!
-- Password for testuser: UserPassword123!
--
COPY public.users (id, username, email, password, role, enabled, created_at, updated_at) FROM stdin;
1	admin	admin@ikaansh.com	$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi	ROLE_ADMIN	t	2026-10-29 10:00:00	2026-10-29 10:00:00
2	testuser	user@ikaansh.com	$2a$10$wOq2M0gG1nZ5Y8jE7mK2Ye0i.R6qE5xW1uY9vB4oG2hN7mK3vP4u	ROLE_USER	t	2026-10-29 10:00:00	2026-10-29 10:00:00
\.

--
-- Data for Name: tasks; Type: TABLE DATA; Schema: public; Owner: postgres
--
COPY public.tasks (id, title, description, due_date, priority, status, completed, user_id, created_at, updated_at) FROM stdin;
1	Setup Spring Boot 3 Architecture	Configure Spring Data JPA, Hibernate, and embedded H2/Postgres profiles	2026-10-31 18:00:00	HIGH	COMPLETED	t	2	2026-10-29 10:05:00	2026-10-29 10:05:00
2	Implement JWT Security & Filter Chain	Implement token extraction, validation, and user scoping	2026-11-01 12:00:00	HIGH	IN_PROGRESS	f	2	2026-10-29 10:10:00	2026-10-29 10:10:00
3	Add Pagination and Sorting to Tasks API	Implement Pageable queries and Swagger OpenAPI documentation	2026-11-03 15:30:00	MEDIUM	PENDING	f	2	2026-10-29 10:15:00	2026-10-29 10:15:00
4	Containerize with Multi-stage Dockerfile	Build lightweight JRE 21 alpine image with non-root user execution	2026-11-05 20:00:00	LOW	PENDING	f	2	2026-10-29 10:20:00	2026-10-29 10:20:00
\.

--
-- Set sequence values
--
SELECT pg_catalog.setval('public.users_id_seq', 2, true);
SELECT pg_catalog.setval('public.tasks_id_seq', 4, true);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);

--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--
ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);

--
-- Indexes
--
CREATE INDEX idx_users_username ON public.users USING btree (username);
CREATE INDEX idx_users_email ON public.users USING btree (email);
CREATE INDEX idx_tasks_user_id ON public.tasks USING btree (user_id);
CREATE INDEX idx_tasks_status ON public.tasks USING btree (status);
CREATE INDEX idx_tasks_priority ON public.tasks USING btree (priority);
CREATE INDEX idx_tasks_due_date ON public.tasks USING btree (due_date);
CREATE INDEX idx_tasks_user_status ON public.tasks USING btree (user_id, status);
CREATE INDEX idx_tasks_user_priority ON public.tasks USING btree (user_id, priority);

--
-- Foreign Key Constraints
--
ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--

