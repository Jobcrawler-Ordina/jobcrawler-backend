INSERT INTO roles(name) VALUES('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles(name) VALUES('ROLE_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Java') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Angular') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Python') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Azure') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'AWS') ON CONFLICT DO NOTHING;
