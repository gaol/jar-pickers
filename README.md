CVE Tracking Project
===========

CVE Tracking project aims to track CVE issues

About source code structure:
=====
src/         -- standard war application structure
db/          -- 'trackers.sql' is the DDL file for CVE database
eap6/        -- Contains postgresql module structure and a EAP cli scripts to configure datasource
tools/       -- Some useful python scripts to interact with CVE database, Bugzilla and brew system.


To combile and deploy the app into EAP6, please follow the steps below:

Build CVE tracker web application
====

> mvn clean install

  Then you will get the `trackers.war` in `target/` directory

Configuration on JBoss EAP 6
====

1. To install org.potgresql module into EAP6:

> cp -r eap6/org [JBOSS-HOME]/modules/system/layers/base/

2. After org.postgresql module is installed, start the EAP instance:

> bin/standalone.sh

3. Then in another console, run the configuration scripts:

> bin/jboss-cli.sh --file=eap6/config-data-source.cli

Deploy
=====

> cp target/trackers.war [JBOSS-HOME]/standalone/deployments/

  This should deploy the trackers web application into EAP6

Then you can visit the apis, which just satisfies mead-scheduler CVE report requirement

1. [CVE Report](http://localhost:8080/trackers/api/cvereport)

2. [Last_Updated](http://localhost:8080/trackers/api/cvereport/last_updated)


Notes on database
=====

   * It was tested working against Postgresql 8.4
   * There is an exist postgresql database set up at host: 10.66.78.40, port: 5432, dbname: trackers, username: trackers, password: trackers
      * JDBC connection url: `jdbc:postgresql://10.66.78.40:5432/trackers`
   * Only two tables are used: `ProductCVE` and `CVELastUpdated`

Notes on cron job to upload report data to tomentum.usersys.redhat.com
=====

  To reduce the possible network problems, we need upload the report data to `tomentum.usersys.redhat.com` periodically.

   * Host: tomentum.usersys.redhat.com
   * Path: `/tmp/runconsist/metadata/report-cve` and `/tmp/runconsist/metadata/report-cve-last_updated`
   * Since we don't have the write permission on this folder, we need LG's help to create symbol link files to some place in your home directory in tomentum.usersys.redhat.com. (Previous created symbol link files have been missing, you need to ask LG to create again, to your own place)
   * You can upload your public key to tomentum.usersys.redhat.com from one of your VM, and set up cron job there.
   * The script used to do the scp job is: `tools/cvereport-sync.py`, you need to modify this file to use the your own report links

Notes on some python scripts
======

  There are some python scripts located at `tools` folder:

   * `work.py`, this provides some handy variables to do staff with Bugzilla / Brew / CVE database. run: `python -i work.py` will open a console for you with some varibles: `cve`, `bugzilla`, `brew`. 
      * To find more detail usage of these 3 varibles, run: `help(cve)` or `help(bugzilla)` or `help(brew)` in your console
      * `utils/cve.py` use 10.66.78.40 as the database host by default, you need to update to your own: `cve.db_host=xxx.xxx.xxx.xxx`
      * python scripts operate on database directly, it does not talk with EAP server
      * written and tested in python 2.7, require: `psycopg2` for postgresql operation; `bugzilla` for bugzilla operation; `brew` for brew operation.

   * `cvereport-sync.py`, this is the script used to scp cve report from EAP to tomentum.usersys.redhat.com, you need to modify it to adapt your folder
   * `utils/*.py`, this is a utils module which provides util methods on CVE database, Bugzilla, Brew, etc. very convenient.  


Notes on Bugzilla account
====

Some embargoed CVE issues may restrict access to only small group of people, you need Fernando's help to ask SRT people to add you in CC for each embargoed CVE bugzilla

