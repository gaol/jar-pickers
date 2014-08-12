#!/usr/bin/python

import sys
import os
import xml.etree.ElementTree as ET

from __init__ import *

class EAP5Picker(Picker):
  """
  Picker whic is used to analyze EAP5 distributions
  For EAP 5, it will try to analyze xml file: `jar-versions.xml`
  """
  def __init__(self):
    super(EAP5Picker, self).__init__()
    self.xmlRoot = None
  #end of init

  def prepare(self, request, dirname):
    for root, dirs, files in os.walk(dirname):
      found = False
      for file in files:
        if file == "jar-versions.xml":
          tree = ET.parse(os.path.join(root, file))
          self.xmlRoot = tree.getroot()
          found = True
          break
      if found:
        break
  #end of prepare

  def getArtifactInfo(self, jar):
    jarFileName = os.path.basename(jar)
    groupId, artifactId, artiVersion = getArtifactInforFromMetaInf(jar)
    if artifactId is None:
      artifactId = jarFileName[:-4]
    artiType = "jar"
    if not artiVersion is None:
      groupId = getGroupId(artifactId, artiVersion)
    if artiVersion is None:
      if self.xmlRoot is None:
        log.error("Can't find jar-versions.xml in EAP 5 zip distribution. Don't know how to analyze the jars information")
        return None
      for j in self.xmlRoot.findall('jar'):
        if j.get('name') == jarFileName:
          artiVersion = j.get('implVersion')
          log.debug("%s: -- %s:%s:%s:%s" % (jarFileName, groupId, artifactId, artiVersion, artiType))
    artiMd5 = md5(jar)
    return groupId, artifactId, artiVersion, artiType, artiMd5
  #end of getArtifactInfo
  
#end of EAP5Picker
if __name__ == '__main__':
  log.info("This is not exectable, please use ./picker.py -n EAP -v 5.x.x -u url for EAP 5")
