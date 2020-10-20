INSERT INTO roles(name) VALUES('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles(name) VALUES('ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Java') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Angular') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Python') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'Azure') ON CONFLICT DO NOTHING;
INSERT INTO skill(id, name) VALUES(uuid_in((md5((random())::text))::cstring), 'AWS') ON CONFLICT DO NOTHING;
CREATE OR REPLACE FUNCTION getDistance(lon1 double precision, lat1 double precision, lon2 double precision, lat2 double precision)
    RETURNS double precision
    LANGUAGE plpgsql
AS '
    DECLARE
        dlon1 double precision;
        dlat1 double precision;
        dlon2 double precision;
        dlat2 double precision;
        dlon double precision;
        dlat double precision;
        a double precision;
        c double precision;
        r double precision;
    BEGIN
        dlon1 := radians(lon1);
        dlat1 := radians(lat1);
        dlon2 := radians(lon2);
        dlat2 := radians(lat2);
        dlon := dlon2 - dlon1;
        dlat := dlat2 - dlat1;
        a := sin(dlat/2)^2 + cos(dlat1) * cos(dlat2) * sin(dlon/2)^2;
        c := 2 * asin(|/a);
        r := 6371;
        RETURN c * r;
    END;
';
