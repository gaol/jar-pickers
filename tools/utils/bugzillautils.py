#!/bin/python

import sys
import getpass
import xmlrpclib

try:
  import bugzilla
except:
  print "No RHBugzilla module found, please install it first!"
  sys.exit(1)

# Red Hat Bugzilla XML-RPC address
XML_RPC_ADDRESS = "https://bugzilla.redhat.com/xmlrpc.cgi"

# Bugzilla Class
class Bugzilla():
  def __init__(self):
    self.uname = None
    self.pawd = None
    self.bz = None

  def bzLogin(self):
    if self.bz is None:
      if self.uname is None or self.pawd is None:
        self.uname = raw_input("Please Enter Your Bugzilla Username: ")
        self.pawd = getpass.getpass("Password: ")
      self.bz = bugzilla.RHBugzilla(url = XML_RPC_ADDRESS)
      self.bz.login(self.uname, self.pawd)
  
  def getBugList(self, queryLink):
    """
     Given a bugzilla search full link, returns a bug list
    """
    self.bzLogin()
    query = self.bz.url_to_query(queryLink)
    return self.bz.query(query)

  def getBug(self, bid):
    """
     Given a bugzilla id or a CVE name, returns a bug 
    """
    self.bzLogin()
    try:
      return self.bz.getbug(bid)
    except xmlrpclib.Fault as f:
      if f.faultCode == 102:
        #print "Fault 102, no permission"
        return None
      raise f

  def getBugProduct(self, bid):
    """
     get's the affected product of a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.product

  def getBugVersion(self, bid):
    """
     get's the first affected version a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.version

  def getBugComponent(self, bid):
    """
     get's the affected component a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.component

  def getBugSummary(self, bid):
    """
     get's the summary of a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.summary

  def getBugTargetReleases(self, bid):
    """
     get's the target release of a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return " ".join(bug.target_release)

  def getBugTargetMileStone(self, bid):
    """
     get's the target milestone of a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.target_milestone

  def getBugStatus(self, bid):
    """
     get's the status of a bugzilla by given bugzilla id
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.status

  def getBugResolution(self, bid):
    """
     get's the resolution of a bugzilla by given bugzilla id, only when status is 'CLOSED', otherwise, None
    """
    bug = self.getBug(bid)
    if bug is None:
      return None
    if "CLOSED" == bug.status:
      return bug.resolution
    return None

  def getBugFixedIn(self, bug):
    """
     get's the composition of target release and target milestone of a bugzilla by given bugzilla id
    """
    if bug is None:
      return None
    target_release = " ".join(bug.target_release)
    target_milestone = bug.target_milestone
    if target_release.strip() == "---" and target_milestone == "---":
      return None
    if target_milestone == "---":
      target_milestone = ""
    if target_release == "---":
      target_release = ""
    return "%s %s" % (target_release, target_milestone)
#end of Bugzilla class
