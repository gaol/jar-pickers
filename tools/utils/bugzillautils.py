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
    self.bzLogin()
    query = self.bz.url_to_query(queryLink)
    return self.bz.query(query)

  def getBug(self, bid):
    self.bzLogin()
    try:
      return self.bz.getbug(bid)
    except xmlrpclib.Fault as f:
      if f.faultCode == 102:
        #print "Fault 102, no permission"
        return None
      raise f

  def getBugProduct(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.product

  def getBugVersion(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.version

  def getBugComponent(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.component

  def getBugSummary(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.summary

  def getBugTargetReleases(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return " ".join(bug.target_release)

  def getBugTargetMileStone(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.target_milestone

  def getBugStatus(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    return bug.status

  def getBugResolution(self, bid):
    bug = self.getBug(bid)
    if bug is None:
      return None
    if "CLOSED" == bug.status:
      return bug.resolution
    return None

  def getBugFixedIn(self, bug):
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
