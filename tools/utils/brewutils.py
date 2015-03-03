#!/bin/python

import brew

# Brew Build system XML-RPC address
XML_RPC_ADDRESS = "http://brewhub.devel.redhat.com/brewhub"

# BrewUtil Class
class BrewUtil():
  def __init__(self):
    self.brewU = brew.ClientSession(XML_RPC_ADDRESS,{})

  def getBuildNVR(self, build):
    build = self.brewU.getBuild(build)
    if build is None:
      return None
    return build["nvr"]

  def getBuild(self, build):
    return self.brewU.getBuild(build)

  def getBuildTaskId(self, build):
    build = self.brewU.getBuild(build)
    if build is None:
      return None
    return build["task_id"]

  def getTask(self, taskId):
    return self.brewU.getTaskInfo(taskId)

  def getTaskMethod(self, taskId):
    task = self.getTask(taskId)
    if task is None:
     return None
    return task["method"]

#end of BrewUtil class

