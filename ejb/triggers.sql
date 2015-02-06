
-- trigger to update latest timestamp
CREATE OR REPLACE FUNCTION last_update_cve()
  RETURNS trigger AS
$$
BEGIN
	UPDATE CVELastUPdated set last_updated = 'now()' where id = 1; 
	RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER last_update_cve_trigger
  AFTER UPDATE ON ProductCVE
  EXECUTE PROCEDURE last_update_cve();