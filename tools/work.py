#!/bin/python

import sys
import os
import optparse
import re
import getpass
import urllib2
import json

dirname = os.path.dirname(sys.argv[0])
if not dirname in sys.path:
  sys.path.append(dirname)

homedir = os.environ['HOME']

# Clear console screen
print(chr(27) + "[2J")


def printColor(color, msg = None):
  if msg is None: pass
  colorcode = "1;32;40m"
  if color == "red":
    colorcode = "1;31;40m"
  elif color == "blue":
    colorcode = "1;34;40m"
  elif color == "yellow":
    colorcode = "1;33;40m"
  print '\033[' + colorcode + msg + '\033[0m'

def info(msg = None):
  printColor("", msg)

def debug(msg = None):
  printColor("blue", msg)

def warn(msg = None):
  printColor("yellow", msg)

def error(msg = None):
  printColor("red", msg)

def loadWorkModules():
  from utils import cve
  global cve
  cve = cve.CVE()
  info ("  CVE module loaded")
  print ""

  from utils import brewutils
  global brew
  brew = brewutils.BrewUtil()
  info ("  BrewUtil module loaded")
  print ""

  from utils import bugzillautils
  global bugzilla
  bugzilla = bugzillautils.Bugzilla()
  info ("  Bugzilla module loaded")
  print ""

def reload():
  loadWorkModules()

print ""

# call initial functions
loadWorkModules()


