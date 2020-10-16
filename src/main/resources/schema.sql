/*create function get_film_count(len_from int, len_to int)
returns int
    language plpgsql
    as '
    declare
        film_count integer;
    begin
        select count(*) into film_count from location where lon between len_from and len_to;
        return film_count;
    end;
';*/

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
