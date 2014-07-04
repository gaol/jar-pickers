-- You can use this file to load seed data into the database using SQL statements

insert into Product (id, name, description, fullName) values (0, 'EAP', 'EAP product seria', 'JBoss Enterprise Application Platform')
insert into Product (id, name, description, fullName) values (1, 'EWS', 'EWS product seria', 'JBoss Enterprise Web Server')
insert into Product (id, name, description, fullName) values (2, 'EWP', 'EWP product seria', 'JBoss Enterprise Web Platform')

-- EAP 4 | 5 | 6
insert into ProductVersion (id, product_id, version, note) values (0, 0, '5.2.0', '')
insert into ProductVersion (id, product_id, version, note) values (1, 0, '6.0.1', '')
insert into ProductVersion (id, product_id, version, note) values (2, 0, '6.1.1', '')
insert into ProductVersion (id, product_id, version, note) values (3, 0, '6.2.0', '')
insert into ProductVersion (id, product_id, version, note) values (4, 0, '6.2.2', '')
insert into ProductVersion (id, product_id, version, note) values (5, 0, '6.2.3', '')
insert into ProductVersion (id, product_id, version, note) values (6, 0, '6.2.3.ER4', '')
insert into ProductVersion (id, product_id, version, note) values (7, 0, '6.2.4', '')

-- EWS 2
insert into ProductVersion (id, product_id, version, note) values (8, 1, '2.0.1', '')
insert into ProductVersion (id, product_id, version, note) values (9, 1, '2.1.0', '')

-- EWP 5
insert into ProductVersion (id, product_id, version, note) values (10, 2, '5.2.0', '')





