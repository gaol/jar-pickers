#!/usr/bin/python
#
#  This scirpt is used to analyze jar information from a zip file or a directory which contains jar files
#
#

import sys
import os
import os.path
import hashlib

import urlgrabber
import urlgrabber.progress
import optparse
import zipfile
import re
import shutil

VERSION_RE = re.compile(r'\-\d')

class logger():
  """
  logger provides to print colorful messages to console
  log = logger()
  log.setDebug() # enable debug level
  log.debug("Debug in Blue")
  log.info("Info in Green")
  log.warn("Warn in Brown")
  log.error("Error in Red")
  """
  COLOR_ERROR = "\033[91m"
  COLOR_INFO = "\033[92m"
  COLOR_WARN = "\033[93m"
  COLOR_DEBUG = "\033[94m"
  COLOR_END = "\033[0m"

  def __init__(self):
    self.debugEnabled = False

  def setDebug(self):
    self.debugEnabled = True

  def noDebug(self):
    self.debugEnabled = False

  def debug(self, msg):
    if self.debugEnabled:
      print "%s[DEBUG] >> %s%s" % (logger.COLOR_DEBUG, msg, logger.COLOR_END)

  def info(self, msg):
    print "%s[INFO] >> %s%s" % (logger.COLOR_INFO, msg, logger.COLOR_END)

  def warn(self, msg):
    print "%s[WARNING] >> %s%s" % (logger.COLOR_WARN, msg, logger.COLOR_END)

  def error(self, msg):
    print "%s[ERROR] >> %s%s" % (logger.COLOR_ERROR, msg, logger.COLOR_END)
# end of class logger

# instance a logger
log = logger()

class Config():
  """
  Configuration on which directory the data will be stored in and read from.
  Default directory is where this script is located.
  """
  BASE_DIR = os.path.dirname(sys.argv[0])

  def __init__(self):
    self.baseDir = Config.BASE_DIR
    self.groupIdFile = "%s/data/products/groupids.ini" % self.baseDir
    if not os.path.exists(self.groupIdFile):
      log.warn("group id file does not exist.")

  def setBaseDir(self, baseDir):
    self.baseDir = baseDir

  def getTmpDir(self):
    return "%s/tmp" % self.baseDir

  def getDataDir(self):
    return "%s/data/products" % self.baseDir

#end of class Config

# instance of Config
config = Config()

def downloadFile(url, output):
  """
  Download file from specified url
  If the `output` exists alreay, it will skip the download
  If the `url` is a local file, then copy it to output
  Return None if any error or the `output` file path
  """
  if os.path.isfile(output):
    log.warn("File: %s exist already. Download skipped." % output)
    return output
  if os.path.isfile(url):
    log.info("URL : %s is a local file, copy it directly." % url)
    shutil.copy(url, output)
    return output
  try:
    log.info('Downloading from %s to file: %s' % (url, output))
    urlgrabber.urlgrab(url, filename=output, progress_obj=urlgrabber.progress.TextMeter())
    log.info('Download completed.' )
    return output
  except:
    log.error("Exception on dowloading the zip file from: '%s', Please check whether the url is valid." % url)
    return None
# end of downloadFile


def unzipFile(zipFile, dir):
  """
  Unzip the zip file to a directory, if the dir is not specified, unzip it to current/tmp directory
  Return None on any error or the dest directory: `dir`
  """
  if not os.path.isfile(zipFile):
    log.error("%s is not a local zip file" % zipFile)
    return None
  try:
    log.info('Unzipping the zip file: %s to directory: %s' % (zipFile, dir))
    zf = zipfile.ZipFile(zipFile, "r")
    zf.extractall(dir)
    zf.close()
    log.info('Unzipping zip file completed')
    return dir
  except:
    log.error("Exception on extracting the zip file: %s" % zipFile)
    return None
# end of unzipFile


def md5(filePath):
  if not os.path.exists(filePath):
    return None
  md5 = hashlib.md5()
  f = open(filePath)
  for line in f:
    md5.update(line)
  f.close
  return md5.digest()

# end of md5

def getJarList(dir, filters=["-jandex.jar", "-javadoc.jar", "-sources.jar"]):
  """
  List all jar files in directory.
  Filters are the strings which endswith that will be filter out.
  """
  jars = []
  for root, dirs, files in os.walk(dir):
    for file in files:
      if file.endswith(".jar"):
        if filters is None:
          jars.append(os.path.join(root, file))
        else:
          addIn = True
          for fl in filters:
            if file.endswith(fl):
              addIn = True
              break
          if addIn:
            jars.append(os.path.join(root, file))
  return jars
# end of getJarList

def getGroupId(artifactId, version):
  """
  Gets groupId according to the artifactId, it is used when the groupId can't be found during the zip parse.
  """
  if not os.path.exists(config.groupIdFile):
    log.debug("GroupIDFile does not exit")
    return None
  f = file(config.groupIdFile, 'r')
  for line in f:
    compK = "%s:%s=" % (artifactId, version[:1])
    if line.startswith(compK):
      return "".join(line[len(compK):].split("\n"))
    if line.startswith("%s=" % artifactId):
      return "".join(line[len(artifactId) + 1:].split("\n"))
  f.close()
  return None
 #end of getGroupId

def parseFromPom(zf, pomFile):
  groupId = None
  artifactId = None
  version = None
  zxf=zf.open(pomFile)
  for line in zxf.readlines():
    if "groupId=" in line:
      groupId = line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
    if "artifactId=" in line:
      artifactId= line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
    if "version=" in line:
      version = line[line.index("=")+ 1:].rstrip('\n').rstrip('\r')
  zxf.close()
  if groupId is None:
    groupId = getGroupId(artifactId, version)
  return groupId, artifactId, version
  
# end of parseFromPom


def parserFromFileName(jar):
  """
  Guess jar groupId, artifactId, version from jar file name.
  This is suitable for jar name: '{artifactId}-{version}.jar'
  return a triple: {groupId, artifactId, version}
  """
  artifactId = None
  version = None
  jarFileName = os.path.basename(jar)[:-4]
  log.debug("Jar file basename is: %s" % jarFileName)
  nameSearch = VERSION_RE.search(jarFileName)
  if not nameSearch is None:
    versionStart = nameSearch.start()
    artifactId = jarFileName[:versionStart]
    version = jarFileName[versionStart + 1:]
  return getGroupId(artifactId, version), artifactId, version
  
# end of parserFromFileName

def guessJarType(jar, artifactId, version):
  """
   Jar name is like: {artifactId}-{version}-{type}.jar
  """
  jarFileName = os.path.basename(jar)[:-4]
  composedFileName = "%s-%s" % (artifactId, version)
  if jarFileName == composedFileName:
    return "jar"
  if jarFileName.startswith(composedFileName):
    return jarFileName[len(composedFileName) + 1:]
  return "jar"
# end of guessJarType


def getArtifactInforFromMetaInf(jar):
  """
   Try to get jar artifact information from META-INF/MANIFEST.MF
  """
  groupId = None
  artifactId = None
  version = None
  try:
    zf=zipfile.ZipFile(jar)
    for name in zf.namelist():
      # parse jar information from pom.properties
      if "MANIFEST.MF" in name:
        log.debug("Find MANIFEST.MF in jar: %s" % jar)
        zxf=zf.open(name)
        for line in zxf.readlines():
          if "groupId =" in line:
            groupId = line[line.index("=")+ 1:].rstrip('\n').rstrip('\r')
          if "artifactId=" in line:
            artifactId= line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
          if "Implementation-Version=" in line:
            version = line[line.index("=") + 1:].rstrip('\n').rstrip('\r')
        zxf.close()
        break
    zf.close()
  except:
    log.error("Error when reading pom.properties from jar: %s " % jar)
  return groupId, artifactId, version
#end of getArtifactInforFromMetaInf


class Picker(object):
  """
  Picker class is used to picks jar information.
  """
  def __init__(self):
    self.config = config
  #end of init

  def setConfig(self, cf):
    self.config = cf

  def getArtifactInfo(self,jar):
    """
    Get artifacts information from a jar file.
    Return a triple of: groupId, artifactId, version, artiType, artiMd5
    """
    groupId = None
    artifactId = None
    version = None
    artiType = "jar"
    log.debug("Try to read pom.properties from jar: %s" % jar)
    try:
      zf=zipfile.ZipFile(jar)
      for name in zf.namelist():
        # parse jar information from pom.properties
        if "pom.properties" in name:
          pomExist = True
          log.debug("Find pom.properties in jar: %s" % jar)
          groupId, artifactId, version = parseFromPom(zf, name)
          break
      zf.close()
    except:
      log.error("Error when reading pom.properties from jar: %s " % jar)

    try:
      if groupId is None or artifactId is None or version is None:
        groupId, artifactId, version = getArtifactInforFromMetaInf(jar)
    except:
      log.error("Error when reading META-INF/MANIFEST from jar: %s " % jar)
  
    if groupId is None or artifactId is None or version is None:
      # try to parse the jar information from file name
      groupId, artifactId, version = parserFromFileName(jar)
    artiType = guessJarType(jar, artifactId, version)
    artiMd5 = md5(jar)
    return groupId, artifactId, version, artiType, artiMd5
  #end of getArtifactInfo

  def prepare(self, request, dirname):
    pass
  #end of prepare

  def picks(self, request):
    """
    Starts to pick jars information up.
    request is the dict data which contains name, version, urls
    name : the name of the product
    version : the version of the product
    urls : the urls where to download the product zip files
    """
    if request is None:
      raise Exception("No request information")
    name = request.get("name", None)
    version = request.get("version", None)
    urls = request.get("urls", [])

    if name is None or version is None or (urls is None or len(urls) == 0):
      raise Exception("Invalid request. name,version,urls must be provided")

    name = name.upper()

    if not os.path.exists(self.config.getTmpDir()):
      log.debug("Download temporary directory does not exist, create it.")
      os.makedirs(self.config.getTmpDir())

    errors = []
    artifacts = []
    for url in urls:
      isLocalFile = False
      isLocalDir = False
      fileName = "%s/%s" % (self.config.getTmpDir(), os.path.basename(url))
      dirName = "%s/tmp-%s" % (self.config.getTmpDir(), os.path.basename(url))
      if os.path.isfile(url):
        log.info("'%s' is a local zip file" % url)
        isLocalFile = True
        fileName = url
      if os.path.isdir(url):
        log.info("'%s' is a local directory" % url)
        isLocalDir = True
        dirName = url
      if isLocalFile or isLocalDir:
        log.info("Skip Downloading")
      else:
        downloadFile(url, fileName)
      if isLocalDir is False:
        unzipFile(fileName, dirName)
      log.info("Starts parsing jar information at directory: '%s'" % dirName)
      self.prepare(request, dirName)
      for jar in getJarList(dirName):
        groupId, artifactId, artiVersion, artiType, artiMd5 = self.getArtifactInfo(jar)
        if artifactId is None or artiVersion is None:
          log.debug("ERROR: Can't parse jar: %s" % jar)
          errors.append("ERROR: when parse : %s" % os.path.basename(jar))
          continue
        if groupId is None:
          groupId = getGroupId(artifactId, artiVersion)
        if groupId is None:
          log.debug("No groupId found in jar file: %s" % os.path.basename(jar))
          errors.append("WARN: No GroupId: %s" % os.path.basename(jar))
        artifactstr = "%s:%s:%s:%s:%s" % (groupId, artifactId, artiVersion, artiType, artiMd5)
        if not artifactstr in artifacts:
          artifacts.append(artifactstr)
      if isLocalDir:
        log.info("Skip Removing")
      else:
        log.info("Remove the template directory: %s" % dirName)
        shutil.rmtree(dirName)

    if not os.path.exists(self.config.getDataDir()):
      log.info("Data directory does not exist, create it.")
      os.makedirs(self.config.getDataDir())
    outputFile = "%s/%s-%s.list" % (self.config.getDataDir(), name, version)
    output = file(outputFile, 'w')
    for artiLine in artifacts:
      output.write(artiLine + "\n")
    output.flush()
    output.close()
    log.info("Checking jars completed. ")
    return errors

  #end of picks

#end of Picker

