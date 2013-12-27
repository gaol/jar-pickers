#!/usr/bin/python

import sys
import os
import os.path

#import urlgrabber

import urllib
import optparse
import zipfile
import re
import shutil
import ConfigParser
import json

VERSION_RE = re.compile(r'\-\d')
DEBUG = False

# location defaults
DOWNLOAD_TMP_DIR = "%s/tmp" % os.getcwd()
DATA_DIR = "%s/data" % os.getcwd()
GROUPID_FILE = "%s/groupids.ini" % DATA_DIR

# RESULT KEY
SUSPECT_JARS = "suspect_jars"
JSON_DATA = "jsonData"

# proudcts file list
PRODUCTS_FILE = "products.json"

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
  if os.path.isfile(output):
    info("File: %s exist already. Download skipped." % output)
    return
  try:
    info('Downloading from %s to file: %s' % (url, output))
    #urlgrabber.urlgrab(url, filename=output, progress_obj=urlgrabber.progress.TextMeter())
    urllib.urlretrieve(url, output)
    info('Download completed.' )
  except:
    info("Exception on dowloading the zip file: %s" % url)
    print sys.exc_info()[0]
# end of downloadZip


def unzipFile(zipFile, dir):
  """
  Unzip the zip file to a directory, if the dir is not specified, unzip it to current/tmp directory
  """
  try:
    info('Unzipping the zip file: %s to directory: %s' % (zipFile, dir))
    zf = zipfile.ZipFile(zipFile, "r")
    zf.extractall(dir)
    zf.close()
    info('Unzipping zip file completed')
  except:
    info("Exception on extracting the zip file: %s" % zipFile)
    print sys.exc_info()[0]
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
  pomExist = False
  debug("Try to read pom.properties from jar: %s" % jar)
  try:
    zf=zipfile.ZipFile(jar)
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
        zxf.close()
    zf.close()
  except:
    info("Error when reading pom.properties from jar: %s " % jar)
    print sys.exc_info()[0]
    pomExist = False

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


def getProductNames(productFile):
  data = json.load(productFile)
  names = []
  for d in data: names.append(d['short_name'])
  return names
#end of getProductNames

class Picker():
  """
  Picker class is used to picks jar information.
  """
  def __init__(self):
    self.tmpDir = DOWNLOAD_TMP_DIR
    self.debug = False
    self.groupIdFile = GROUPID_FILE
    self.dataDir = DATA_DIR
  #end of init

  def setTmpDir(self, tmpDir):
    if not tmpDir is None:
      self.tmpDir = tmpDir
  #end

  def setDebug(self, debug):
    if not debug is None:
      self.debug = debug
  #end

  def setGroupIdFile(self, groupIdFile):
    if not groupIdFile is None:
      self.groupIdFile = groupIdFile
  #end

  def setDataDir(self, dataDir):
    if not dataDir is None:
      self.dataDir = dataDir
  #end

  def getGroupId(self, artifactId, version):
    """
    Gets groupId according to the artifactId, it is used when the groupId can't be found during the zip parse.
    """
    config = ConfigParser.ConfigParser()
    if not os.path.exists(self.groupIdFile):
      info("group id file does not exist.")
      return None
    config.read(self.groupIdFile)
    try:
      return config.get('groupids', "%s_%s" % (artifactId, version[:1]))
    except ConfigParser.NoOptionError:
      try:
        return config.get('groupids', artifactId)
      except ConfigParser.NoOptionError:
        return None
  #end of getGroupId


  def isNameExist(self, name):
    """
    Check whether the name will be supported for the collection.
    The name will be capitalized.
    """
    if not os.path.exists(self.dataDir):
      info("Data directory does not exist, create it.")
      os.makedirs(self.dataDir)
    productsFile = file("%s/%s" % (this.dataDir, PRODUCTS_FILE), 'r')
    return name in getProductNames(productsFile)
  # end of isNameExist

  def picks(self, request):
    """
    Starts to pick jars information up.
    request is the dict data which contains name, version, milestone, urls
    name : the name of the product
    version : the version of the product
    milestone : the milestone of the product, this is optional
    urls : the urls where to download the product zip files
    """
    if request is None:
      raise Exception("No request information")
    name = request.get("name", None)
    version = request.get("version", None)
    milestone = request.get("milestone", None)
    urls = request.get("urls", [])

    if name is None or version is None or (urls is None or len(urls) == 0):
      raise Exception("Invalid request. name,version,urls must be provided")
    
    name = name.upper()
    if not isNameExist(name): raise Exception("%s is not supported yet, contact administrator to add it please." % name)

    if not os.path.exists(self.tmpDir):
      info("Download temporary directory does not exist, create it.")
      os.makedirs(self.tmpDir)

    result = {SUSPECT_JARS : []}
    artifacts = []
    for url in urls:
      fileName = "%s/%s" % (self.tmpDir, os.path.basename(url))
      downloadZip(url, fileName)
      dirName = "%s/tmp-%s" % (self.tmpDir, os.path.basename(url))
      unzipFile(fileName, dirName)
      for jar in getJarList(dirName):
        groupId, artifactId, artiVersion = getArtifactInfo(jar)
        if artifactId is None or artiVersion is None:
          info("WARNING: Can't parse jar: %s" % jar)
          result[SUSPECT_JARS].append("Error: %s" % os.path.basename(jar))
          break
        if groupId is None:
          groupId = self.getGroupId(artifactId, artiVersion)
        if groupId is None:
          info("No groupId found in jar file: %s" % os.path.basename(jar))
          result[SUSPECT_JARS].append("No GroupId: %s" % os.path.basename(jar))
        artifactstr = "%s:%s:%s" % (groupId, artifactId, artiVersion)
        if not artifactstr in artifacts:
          artifacts.append(artifactstr)
      info("Remove the template directory: %s" % dirName)
      shutil.rmtree(dirName)

    if not os.path.exists(self.dataDir):
      info("Data directory does not exist, create it.")
      os.makedirs(self.dataDir)
    outputFile = "%s/%s-%s-%s.json" % (self.dataDir, name, version, milestone)
    if milestone is None:
      outputFile = "%s/%s-%s.json" % (self.dataDir, name, version)
    output = file(outputFile, 'w')
    jsonData = {"name" : name, "version" : version, "milestone" : milestone, "urls" : urls, "artifacts" : artifacts}
    result[JSON_DATA] = jsonData
    json.dump(jsonData, output, indent = 2)
    output.flush()
    output.close()
    info("Checking jars completed. ")
    return result

  #end of picks

#end of Picker

def listValues(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))
#end of listValues

def main():
  """
   Main entrance for command line.
  """
  parser = optparse.OptionParser(usage='%prog [options]')
  parser.add_option('--debug', dest='debug', help='Print debug message', action='store_true', default = True)
  parser.add_option('-n', '--name', dest='name', help='Specify JBoss product short name. Like: eap, ews, etc.')
  parser.add_option('-v', '--version', dest='version', help='Specify JBoss product version')
  parser.add_option('-m', '--milestone', dest='milestone', help='Specify JBoss product milestone')
  parser.add_option('-u', '--urls', dest='urls', type='string', help='Specify zip files url.', action='callback', callback=listValues)
  options, args = parser.parse_args()
  debug = options.debug
  name = options.name
  version = options.version
  milestone = options.milestone
  urls= options.urls
  if name is None:
    parser.print_help()
    sys.exit(1)
  picker = Picker()
  print picker.picks({"name" : name, "version" : version, "milestone" : milestone, "urls" : urls})
#end of main


if __name__ == '__main__':
  main()
