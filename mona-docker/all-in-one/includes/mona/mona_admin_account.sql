--
-- Data for Name: submitter; Type: TABLE DATA; Schema: public; Owner: mona
--

INSERT INTO submitter (id, account_enabled, account_expired, account_locked, date_created, email_address, first_name, institution, last_name, last_updated, password, password_expired) VALUES (4, true, false, false, '2016-02-09 23:25:32.387', 'admin@mona', 'Admin', 'MoNA', 'User', '2016-02-09 23:25:32.387', '$2a$10$1wVZMDY/dCtvGEjYq5KY.OSZJU3N5341IQZvxfpjtj.G2F1ngVTSC', false);

--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: mona
--

INSERT INTO role (id, version, authority) VALUES (1, 0, 'ROLE_ADMIN');
INSERT INTO role (id, version, authority) VALUES (2, 0, 'ROLE_CURATOR');
INSERT INTO role (id, version, authority) VALUES (3, 0, 'ROLE_USER');

--
-- Data for Name: submitter_role; Type: TABLE DATA; Schema: public; Owner: mona
--

INSERT INTO submitter_role (role_id, submitter_id) VALUES (1, 4);
INSERT INTO submitter_role (role_id, submitter_id) VALUES (2, 4);
INSERT INTO submitter_role (role_id, submitter_id) VALUES (3, 4);