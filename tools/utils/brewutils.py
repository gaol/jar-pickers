#!/bin/python

import brew

# Brew Build system XML-RPC address
XML_RPC_ADDRESS = "http://brewhub.devel.redhat.com/brewhub"

# BrewUtil Class
class BrewUtil():
  def __init__(self):
    self.brewU = brew.ClientSession(XML_RPC_ADDRESS,{})

  def getBuildNVR(self, build):
    """
    Gets NVR of a build, by give it the build id
    """
    build = self.brewU.getBuild(build)
    if build is None:
      return None
    return build["nvr"]

  def getBuild(self, build):
    """
    Returns the Build object by the nvr or build id
    """
    return self.brewU.getBuild(build)

  def getBuildTaskId(self, build):
    """
    Gets the task if of the build
    """
    build = self.brewU.getBuild(build)
    if build is None:
      return None
    return build["task_id"]

  def getTask(self, taskId):
    """
    Gets the task infomation by given a task id(integer)
    """
    return self.brewU.getTaskInfo(taskId)

  def getTaskMethod(self, taskId):
    """
    Gets the task method by given a task id.
    """
    task = self.getTask(taskId)
    if task is None:
     return None
    return task["method"]

#end of BrewUtil class

