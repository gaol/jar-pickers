-- You can use this file to load seed data into the database using SQL statements

insert into Product (id, name, description, fullName) values (0, 'EAP', 'EAP product seria', 'JBoss Enterprise Application Platform')
insert into Product (id, name, description, fullName) values (1, 'EWS', 'EWS product seria', 'JBoss Enterprise Web Server')
insert into Product (id, name, description, fullName) values (2, 'EWP', 'EWP product seria', 'JBoss Enterprise Web Platform')
insert into Product (id, name, description, fullName) values (3, 'BRMS', 'Business Rule Management System', 'JBoss BRMS')

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

-- BRMS 6
insert into ProductVersion (id, product_id, parent_id, version, note) values (11, 3, 7, '6.2.0', '')

-- sample components
insert into Component (id, name, version, groupid, description, scm) values (0, 'picketlink', '7.2.0.Final', 'org.picketlink', '', 'https://github.com/jbossas/picketlink')

-- sample native components
insert into Component (id, name, version, description, scm, isnative) values (1, 'mod_cluster-native', '1.2.9.Final-redhat-1', '', 'https://github.com/modcluster/mod_cluster', 'true') 
insert into Component (id, name, version, description, scm, isnative) values (2, 'openssl', '1.0.1', '', 'https://git.openssl.org', 'true') 

-- sample artifacts in EWP 5.2.0
insert into Artifact (id, component_id, groupid, artifactid, version, buildinfo, type, note) values (0, 0,'org.jboss.as','jboss-as-picketlink','7.2.0.Final-redhat-3','','jar','')
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (1, 'org.jboss.as','jboss-as-security','7.2.0.Final-redhat-3','','jar','')

insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (2, 'org.jboss.ironjacamar','ironjacamar-common-api','1.0.3.Final','','jar','')
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (3, 'org.jboss.ironjacamar','ironjacamar-common-api','1.0.2.Final','','jar','')
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (4, 'org.jboss.ironjacamar','ironjacamar-common-impl','1.0.2.Final','','jar','')
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (5, 'org.jboss.ironjacamar','ironjacamar-common-impl','1.0.3.Final','','jar','')

-- 2 artifacts with differnt groupIds
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (6, 'javax.jsf','jsf-impl','1.0.2','','jar','')
insert into Artifact (id, groupid, artifactid, version, buildinfo, type, note) values (7, 'com.sun.jsf','jsf-impl','2.0.1','','jar','')


insert into ProductVersion_Artifact (pvs_id, artifacts_id) values (10, 0)
insert into ProductVersion_Artifact (pvs_id, artifacts_id) values (10, 1)

insert into ProductVersion_Artifact (pvs_id, artifacts_id) values (7, 3)
insert into ProductVersion_Artifact (pvs_id, artifacts_id) values (7, 4)

insert into ProductVersion_Component (pvs_id, comps_id) values (7, 1)
insert into ProductVersion_Component (pvs_id, comps_id) values (11, 1)

-- example: EAP 6.2.0 has openssl 1.0.1
insert into ProductVersion_Component (pvs_id, comps_id) values (3, 2)

-- cve data
insert into cve (name) values ('CVE-2014-3547')
insert into cve (name) values ('CVE-2012-3645')
insert into cve (name) values ('CVE-2013-0017')
insert into cve (name) values ('CVE-2014-3566')


insert into AffectedArtifact (id, artiGrpId, artiId, versionScopes) values (1, 'org.jboss.ironjacamar', 'ironjacamar-common-impl', '<=1.0.2.Final')
insert into AffectedArtifact (id, artiId, versionScopes) values (2, 'openssl', '<=1.0.1')

insert into CVE_AffectedArtifact (cves_name, affectedArtis_id) values ('CVE-2014-3547', 1)
insert into CVE_AffectedArtifact (cves_name, affectedArtis_id) values ('CVE-2014-3566', 2)



