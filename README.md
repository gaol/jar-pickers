Trackers Project
===========

Trackers project aims to track all information of EAP and layered products like:

   * Jar information
      * groupId, artifactId, version of each jar
      * which package does this jar belongs to
      * Which build produces this jar

   * Product Information
      * Which build produces this product(including the full build command line)
      * How many versions for a specific product
      * What jars each product version has

   * CVE issues
      * Which CVEs affect our products
      * Which jars does a specfic CVE affect
      * In which jar version does this CVE is fixed

Test
=====

   * Prerequisite
      * JDK 1.6+
      * JavaEE 6+
      * Set up `jbossHome`, `/softwares/eap6/fortest` is used by default.
         * Modify the `src/test/resources/arqullian.xml` to change the `jbossHome` to a your local JBossAS installation
      * Set up test account for the update operations
         * username: `test`, password: `test1#pwd` is used during unit test
            * <JBossHome>bin/adduser.sh can be used to do that
         * groups: `tracker`

   * Run:

> mvn -Parq-jbossas-managed clean test


Build
=====

Run:

> mvn clean install

  Then you will get the `trackers.war` in `data-service/target/` directory

Deploy
=====

   * Prerequisite
      * Set up database (currently support `Postgresql`)
         * Install database, and then create a user(like: `tracker`) which owns a database(like: `tracker`).
         * Login the created database, then import the DDL: `\i /sources/trackers/data-service/trackers.sql`
      * Add users which have `tracker` role via command: `<JBOSS-HOME>bin/adduser.sh`
      * Set up postgresql jdbc drvier and datasource

> You can read documents in [Wiki](https://github.com/gaol/trackers/wiki) to know how to set up the environment step by step.

  After all above is done, you are ready to deploy the `trackers.war`, just throw it into the `<JBOSS-HOME>/standalone/deployments/` will be fine.


