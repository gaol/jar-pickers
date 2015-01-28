#!/bin/python

#
#  Script to collect CVE issues exported from Bugzilla CSV file.
#

import sys
import os
import optparse
import re

import csv

CVE_RE = re.compile(r'CVE\-\d\d\d\d\-\d\d\d\d')


def printSQLS(csvFile):
  reader = csv.reader(open(csvFile))
  for id, alias,title in reader:
    # insert into cve(name, alias, title) values ('CVE-2014-3567', 'CVE-2014-3427', 'XSS');
    name = None
    for n in alias.split(","):
      if CVE_RE.match(n):
        name = n
        break
    ar = alias.split(",")
    if not name is None:
      ar.remove(name) 
    aliasStr = ",".join(ar)
    print "insert into cve(name, alias, title) values ('%s', '%s', '%s');" % (name, aliasStr, title)
#end of printSQLS


def listValues(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))
#end of listValues

def main():
  """
   Main entrance for command line.
  """
  usage="%prog [options]"
  description="""  csvFile must be specified. CSV file format: id, alias, title """
  parser = optparse.OptionParser(usage=usage, description = description)
  parser.add_option('-f', '--file', dest='csvFile', help='CSVFile exported from BugZilla. This is required')
  options, args = parser.parse_args()
  csvFile = options.csvFile
  if csvFile is None:
    parser.print_help()
    sys.exit(1)
  printSQLS(csvFile)
#end of main

if __name__ == '__main__':
  dirname = os.path.dirname(sys.argv[0])
  if not dirname in sys.path:
    sys.path.append(dirname)
  main()
