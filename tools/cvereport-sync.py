#!/usr/bin/python

import sys
import os
import urllib2

REMOTE="lgao@tomentum.usersys.redhat.com:cvereport/"

CVE_REPORT_URL="http://10.66.78.40:8080/trackers/api/cvereport"

CVE_REPORT_LAST_UPDATED_URL="http://10.66.78.40:8080/trackers/api/cvereport/last_updated"

homedir = os.environ['HOME']
cvereportDir = "%s/cvereport/" % homedir

if not os.path.exists(cvereportDir):
  print "Creating %s" % cvereportDir
  os.mkdir(cvereportDir)
  

cvereport = urllib2.urlopen(CVE_REPORT_URL)
cvereportContent = cvereport.read()

if cvereport.code == 200 and cvereport.headers["Content-Type"] == 'application/json':
  # good, save cvereport
  fw = open("%sreport-cve" % cvereportDir, 'w')
  fw.write(cvereportContent)
  fw.close()


last_updated = urllib2.urlopen(CVE_REPORT_LAST_UPDATED_URL)
content_last_updated = last_updated.read()

if last_updated.code == 200 and last_updated.headers["Content-Type"] == 'application/json':
  # good, save cvereport_last_updated
  fw = open("%sreport-cve-last_updated" % cvereportDir, 'w')
  fw.write(content_last_updated)
  fw.close()

print "OK, data has been downloaded, now scp to remote."

scp_commands = "scp -r %s %s" % (cvereportDir, REMOTE)

print scp_commands

os.system(scp_commands)

print "great, data has been uploaded."

