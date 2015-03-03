
drop table if exists CVELastUpdated cascade;

drop table if exists ProductCVE cascade;

create table CVELastUpdated (
    id int4 not null,
    last_updated timestamp,
    primary key (id)
);

create table ProductCVE (
    id  bigserial not null,
    cve varchar(255),
    name varchar(255),
    version varchar(255),
    component varchar(255),
    bugzilla varchar(255),
    bugzillaStatus varchar(255),
    build varchar(1024),
    errata varchar(255),
    fixedIn varchar(255),
    note varchar(255),
    primary key (id)
);

-- unique constraint
alter table ProductCVE 
        add constraint cve_bugzilla_name_version_component unique (cve, name, version, component, bugzilla);

-- initial data in CVELastUpdated table
insert into CVELastUPdated (id, last_updated) values(1, 'now()');

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

