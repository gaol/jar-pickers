#!/bin/python

import sys
import os
import optparse
import re

try:
  import psycopg2
  import psycopg2.extras
except:
  print "No psycopy2 module installed, can't do anything postgresql database related operations!!"
  sys.exit(1)

#import brewutils
import bugzillautils

# CVE pattern
CVE_RE = re.compile(r'CVE\-\d\d\d\d\-\d\d\d\d')

# Name in Bugzilla and product code
NAME_DICT = {"JBoss Enterprise Application Platform 7" : "eap7",
             "JBoss Enterprise Application Platform 6" : "eap6",
             "JBoss Enterprise Application Platform 5" : "eap5",
             "JBoss Enterprise Application Platform 4" : "eap4",
             "JBoss Enterprise Web Server 2" : "ews2",
             "JBoss Enterprise Web Server 1" : "ews1",
             "JBoss Web Server 3" : "jws3",
            }

# URL to list all security tracking bugzillas
URL_CVE_LIST = "https://bugzilla.redhat.com/buglist.cgi?bug_status=NEW&bug_status=ASSIGNED&bug_status=POST&bug_status=MODIFIED&bug_status=ON_DEV&bug_status=ON_QA&classification=JBoss&keywords=Security%2C%20SecurityTracking%2C%20&keywords_type=anywords&known_name=All%20CVEs&list_id=3262643&product=JBoss%20Enterprise%20Application%20Platform%204&product=JBoss%20Enterprise%20Application%20Platform%205&product=JBoss%20Enterprise%20Application%20Platform%206&product=JBoss%20Enterprise%20Web%20Server%201&product=JBoss%20Enterprise%20Web%20Server%202&product=JBoss%20Web%20Server%203&query_based_on=All%20CVEs&query_format=advanced"

# URL to list all umbrella CVE issues
URL_CVE_UMBRELLA_LIST = "https://bugzilla.redhat.com/buglist.cgi?bug_status=NEW&bug_status=ASSIGNED&bug_status=POST&bug_status=MODIFIED&bug_status=ON_DEV&bug_status=ON_QA&classification=Other&component=vulnerability&product=Security%20Response&status_whiteboard=jboss&status_whiteboard_type=allwordssubstr"

def filterCVE(cves):
  """
    Filter all CVE names form a list, which elements matches CVE regex
    Return a tuple, first is the CVE names list, second is the other list contains other elements
  """
  if cves is None:
    return None, None
  first = filter(lambda cve: CVE_RE.match(cve), cves)
  if not first is None:
    first.sort()
    first.reverse()
  second = filter(lambda cve: not CVE_RE.match(cve), cves)
  if not second is None:
    second.sort()
    second.reverse()
  return first, second

# CVE Class
class CVE():
  def __init__(self):
    """
    You can update cve database connection by:
    cve.db_host=xxx.xxx.xxx.xxx
    cve.db_port=5432
    cve.db_name=trackers
    cve.db_user=trackers
    """
    self.db_host = "10.66.78.40"
    self.db_port = 5432
    self.db_name = "trackers"
    self.db_user = "trackers"
    #self.brew = brewutils.BrewUtil()
    self.bugzilla = bugzillautils.Bugzilla()

  def setBugzillaLogin(self, uname, pawd):
    """
    Sets Bugzilla login, for example:
    cve.setBugzillaLogin("lgao@redhat.com", "my-bugzilla-password")

    If you don't set the Bugzilla Login, it will prompt you for input, setBugzillLogin can be used in a batch cli
    """
    self.bugzilla.uname = uname
    self.bugzilla.pawd = pawd

  # Gets CVE database connection
  def getCVEDBConn(self):
    """
     Gets a connection to the CVE database
    """
    return psycopg2.connect(database = self.db_name, user = self.db_user, host = self.db_host, port = self.db_port)

  # update each CVE bugzilla status and fixed_in_version (target_release)
  def cveUpdateAll(self):
    """
      It iterates all CVE information in ProductCVE table, update those item that bugzillaStatus != 'CLOSED'

      It will Update bugzilla status, target_release, target_milestone.

      you can invoke it like:

      cve.cveUpdateAll()
    """
    conn = self.getCVEDBConn()
    cur = conn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    cur.execute("select * from productcve order by cve desc")
    for row in cur.fetchall():
      self._handleCVERow(row)
    conn.close()

  def _handleCVERow(self, row):
    """
      This handles each productcve row to update or delete
      For those WONTFIX, NOTABUG, DUPLICATE CVE tracking bugzillas, they will be delete on each run
      For those Non-CLOSED CVE tracking bugzillas, it will update bugzilla status and target_release/target_milestone
      TODO: it does not update builds and erratas for now.
            and for builds/erratas, they maybe n:1 for each CVE tracking bugzilla
    """
    if not row["bugzillastatus"] is None and row["bugzillastatus"].find("CLOSED") >= 0:
      if row["bugzillastatus"].find("WONTFIX") >= 0 or row["bugzillastatus"].find("NOTABUG") >= 0 or row["bugzillastatus"].find("DUPLICATE") >= 0:
        # delete id
        sql = "delete from productcve where id = %s" % row["id"]
        self.executeCVEDBSQL(sql)
      # returns anyway for CLOSED bugzilla
      return
    bzid = row["bugzilla"]
    try:
      bzid = int(bzid)
    except ValueError:  # it is possible it is an external Jira link, in that case, bugzilla is '-', and will be noted in note field
      print "Unsupported bugzilla: %s" % bzid
      return
    print "Checking CVE: %s with Bugzilla: %s" % (row["cve"], bzid)
    bug = self.bugzilla.getBug(bzid)
    rid = row["id"]
    # update status
    bzstatus = bug.status
    if bzstatus == "CLOSED":
      bzstatus = "%s %s" % (bzstatus, bug.resolution)
    sql = "update productcve set bugzillastatus = '%s' where id = %s" % (bzstatus, rid)
    self.executeCVEDBSQL(sql)
    fixedIn = self.bugzilla.getBugFixedIn(bug)
    # update fixed_in_version only if the bugzulla status is "VERIFIED" or "CLOSED" or ""
    # if bug.status == "VERIFIED" or bug.status == "CLOSED" or bug.status == ""
    sql = "update productcve set fixedIn = '%s' where id = %s" % (fixedIn, rid)
    self.executeCVEDBSQL(sql)

  def executeCVEDBSQL(self, sql):
    """
      If you have a sql want to execute(update/delete) directly in CVE database, call this method:
  
      sql = "delete from productcve where cve is null"
      cve.executeCVEDBSQL(sql)
    """
    conn = self.getCVEDBConn()
    cur = conn.cursor()
    cur.execute(sql)
    conn.commit()
    conn.close()
    
  def cveUpdateTop10(self):
    """
      It iterates all CVE information in productcve table. The latest 10 CVEs, order by CVE number desc
    """
    conn = self.getCVEDBConn()
    cur = conn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    # limit CVE number to 10, but the result number may be larger than that
    sql = "select pc1.* from productcve pc1 inner join (select distinct(cve) from productcve order by cve desc limit 10) as pc2 on pc1.cve = pc2.cve order by pc1.cve desc"
    cur.execute(sql)
    for row in cur.fetchall():
      self._handleCVERow(row)
    conn.close()
  
  def collect(self):
    """
      Collects CVEs using a small bugzilla search
    """
    bzList = self.bugzilla.getBugList(URL_CVE_LIST)
    for bug in bzList:
      cves = self.extractCVEs(bug.summary)

      if len(cves) > 0:
        cves.sort()
        cves.reverse()
        cve = cves[0]
        others = " "
        if len(cves) > 1:
          others = " ".join(cves[1:])
        # there is no way to know from summary the exactly CVE name, like it contains string: "incomplete fix for CVE-xxxx-xxxx"
        # so only picks up the higher CVE number, and leave others in note
        self._recordCVEBug(cve, bug, others)
      else:
        # no cve names found in summary
        print "No CVE names found in summary: '%s' of bugzilla: %s" % (bug.summary, bug.id)

  def extractCVEs(self, title):
    """
     Returns a CVE name list from a string, usually it means the titile in the bugzilla

     For example: print cve.extractCVEs("CVE-2014-0058 Red Hat JBoss EAP6: Plain text password logging during security audit")
    """
    return CVE_RE.findall(title)

  def _recordCVEBug(self, cve, bug, note = None):
    if cve is None or bug is None:
      print "Both CVE and bug can't be None"
      return
    name = bug.product
    if NAME_DICT.has_key(bug.product):
      name = NAME_DICT[bug.product]
    else:
      #print "Not intested product name: %s of CVE: %s on bugzilla: %s" % (bug.product, cve, bug.id)
      return
    # check whether recorded already!
    sql = "select count(*) from productcve where cve = '%s' and name = '%s' and version = '%s' and component = '%s' and bugzilla = '%s'" % (cve, name, bug.version, bug.component, bug.id)
    conn = self.getCVEDBConn()
    cur = conn.cursor(cursor_factory = psycopg2.extras.DictCursor)
    cur.execute(sql)
    count = cur.fetchone()[0]
    conn.close()
    if count > 0: # recorded already, return
      return
    # insert new record
    sql = "insert into productcve(cve, name, version, component, bugzilla, bugzillastatus, fixedIn) values ('%s', '%s', '%s', '%s', '%s', '%s', '%s');" % (cve, name, bug.version, bug.component, bug.id, bug.status, self.bugzilla.getBugFixedIn(bug))
    if not note is None:
      sql = "insert into productcve(cve, name, version, component, bugzilla, bugzillastatus, fixedIn, note) values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');" % (cve, name, bug.version, bug.component, bug.id, bug.status, self.bugzilla.getBugFixedIn(bug), note)
    self.executeCVEDBSQL(sql)


  def checkCVE(self, cve, bug = None, note = None):
    """
     If you want to check single CVE, you can call this method:

     cve.checkCVE("CVE-2014-0058")
    """
    m = CVE_RE.match(cve)
    if not m:
      print "Not a valid CVE name: %s" % cve
      return
    cve = m.group()
    if bug is None:
      bug = self.bugzilla.getBug(cve)
    if bug is None:
      print "Can't access %s" % cve
      sys.exit()
    depends = bug.depends_on
    if not depends is None:
      for dep in depends:
        depData = self.bugzilla.getBug(dep)
        if not depData is None:
          self._recordCVEBug(cve, depData, note)

  def collectCVEs(self):
    """
      Collects CVEs using a umbreall bugzilla search
    """
    bzList = self.bugzilla.getBugList(URL_CVE_UMBRELLA_LIST)
    for bug in bzList:
      alias = bug.alias
      if alias is None:
        print "No CVE name found from: %s" % bug.summary
        continue
      cves,noncves = filterCVE(alias)
      if not cves is None:
        if len(cves) > 0:
          cve = cves[0]
          note = None
          if not noncves is None:
            note = " ".join(noncves)
          if len(cves) > 1:
            if note is None:
              note = " ".join(cves[1:])
            else:
              note = "%s %s" % (note, " ".join(cves[1:]))
          #print "Checking CVE: %s with note: %s" % (cve, "" + note)
          self.checkCVE(cve, bug, note)

#end of CVE class
