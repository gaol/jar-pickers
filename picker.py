#!/bin/python

import sys
import os
import os.path
import urlgrabber
import urlgrabber.progress
import optparse
import zipfile
import re
import shutil
import yaml
import ConfigParser


DOWNLOAD_TMP_DIR = "tmp"
DEBUG = False
VERSION_RE = re.compile(r'\-\d')

def debug(message):
  if DEBUG == True:
    print message
#end of debug

def info(message):
  print message
#end of info

def downloadZip(url, output):
  """
  Downloads zip file from specified zip url
  """
  try:
      if os.path.isfile(output):
        info("File: %s exist already. Download skipped." % output)
        return
      info('Downloading from %s to file: %s' % (url, output))
      urlgrabber.urlgrab(url, filename=output, progress_obj=urlgrabber.progress.TextMeter())
      info('Download completed.' )
  except:
      info('Could not download %s.' % url)
      print sys.exc_info()[0]
      sys.exit(1)
# end of downloadZip


def unzipFile(zipFile, dir):
  """
  Unzip the zip file to a directory, if the dir is not specified, unzip it to current/tmp directory
  """
  try:
    info('Unzipping the zip file: %s to directory: %s' % (zipFile, dir))
    zf = zipfile.ZipFile(zipFile, "r")
    zf.extractall(dir)
  except:
    info("Can't unzip the zip file: %s" % zipFile)
    print sys.exec_info()[0]
    sys.exit(1)
# end of unzip


def getJarList(dir):
  """
  List all jar files in directory.
  """
  jars = []
  for root, dirs, files in os.walk(dir):
    for file in files:
      if file.endswith(".jar") and not file.endswith("-jandex.jar") and not file.endswith("-javadoc.jar") and not file.endswith("-sources.jar"):
        jars.append(os.path.join(root, file))
  return jars
# end of getJarList


def getArtifactInfo(jar):
  """
  Get artifacts information from a jar file.
  Return a triple of: groupId, artifactId, version
  """
  groupId = None
  artifactId = None
  version = None
  zf=zipfile.ZipFile(jar)
  pomExist = False
  debug("Try to read pom.properties from jar: %s" % jar)
  for name in zf.namelist():
    # find pom.properties
    if "pom.properties" in name:
      debug("Find pom.properties in jar: %s" % jar)
      zxf=zf.open(name)
      pomExist = True
      for line in zxf.readlines():
        if "groupId=" in line:
          groupId = line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
        if "artifactId=" in line:
          artifactId= line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
        if "version=" in line:
          version = line[line.index("=")+ 1:].rstrip('\n').rstrip('\r')

  if pomExist is False:
    jarFileName = os.path.basename(jar)[:-4]
    debug("Jar file basename is: %s" % jarFileName)
    debug("No pom.properties found in jar: %s" % jar)
    nameSearch = VERSION_RE.search(jarFileName)
    if not nameSearch is None:
      versionStart = nameSearch.start()
      artifactId = jarFileName[:versionStart]
      version = jarFileName[versionStart + 1:]
  return groupId, artifactId, version
#end of getArtifactInfo

def getGroupId(artifactId, version):
  """
  Gets groupId according to the artifactId, it is used when the groupId can't be found during the zip parse.
  """
  config = ConfigParser.ConfigParser()
  config.read("data/groupids.ini")
  try:
    return config.get('groupids', "%s_%s" % (artifactId, version[:1]))
  except ConfigParser.NoOptionError:
    try:
      return config.get('groupids', artifactId)
    except ConfigParser.NoOptionError:
      return None
#end of getGroupId

def listValues(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))
#end of listValues

class ArtifactBills(yaml.YAMLObject):
  """
   Class which contains information for yaml dump.
  """
  yaml_tag = u'!ArtifactBills'
  def __init__(self, name, version, milestone, urls, artifacts):
    self.name = name
    self.version = version
    self.milestone = milestone 
    self.urls = urls 
    self.artifacts = artifacts

#end of class ArtifactBills

def main():
  """
   Main entrance.
  """
  parser = optparse.OptionParser(usage='%prog [options]')
  parser.add_option('--debug', dest='debug', help='Print debug message', action='store_true', default = True)
  parser.add_option('-n', '--name', dest='name', help='Specify JBoss product short name. Like: eap, ews, etc.')
  parser.add_option('-v', '--version', dest='version', help='Specify JBoss product version')
  parser.add_option('-m', '--milestone', dest='milestone', help='Specify JBoss product milestone')
  parser.add_option('-u', '--urls', dest='urls', type='string', help='Specify zip files url.', action='callback', callback=listValues)
  options, args = parser.parse_args()
  DEBUG = options.debug
  name = options.name
  prdVersion = options.version
  milestone = options.milestone
  urls= options.urls
  if name is None or prdVersion is None or (urls is None or len(urls) == 0):
    parser.print_help()
    sys.exit(1)
    
  if not os.path.exists(DOWNLOAD_TMP_DIR):
    debug("Download temporary directory does not exist, create it.")
    os.makedirs(DOWNLOAD_TMP_DIR)
  
  artifacts = []
  for url in urls:
    fileName = "%s/%s" % (DOWNLOAD_TMP_DIR, os.path.basename(url))
    downloadZip(url, fileName)
    dirName = "%s/tmp-%s" % (DOWNLOAD_TMP_DIR, os.path.basename(url))
    unzipFile(fileName, dirName)
    for jar in getJarList(dirName):
      groupId, artifactId, version = getArtifactInfo(jar)
      if artifactId is None or version is None:
        info("WARNING: Can't parse jar: %s" % jar)
        break
      if groupId is None:
        groupId = getGroupId(artifactId, version)
      if groupId is None:
        info("No groupId found in jar file: %s" % os.path.basename(jar))
      artifactstr = "%s:%s:%s" % (groupId, artifactId, version)
      if not artifactstr in artifacts:
        artifacts.append(artifactstr)
    shutil.rmtree(dirName)

  artifactBills = ArtifactBills(name, prdVersion, milestone, urls, artifacts)
  outputFile = "data/%s-%s-%s.yaml" % (name, prdVersion, milestone)
  if milestone is None:
    outputFile = "data/%s-%s.yaml" % (name, prdVersion)
  output = file(outputFile, 'w')
  yaml.dump(artifactBills, output, default_flow_style=False )
  info("Checking jars completed. ")
#end of main

main()
