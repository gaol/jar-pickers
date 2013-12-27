#!/usr/bin/python
#
#  This is the listen verticle used to listen to the event to pick jars up
#
import os
import vertx
from core.event_bus import EventBus
from picker import Picker

# This is the address registred to listen which will trigger the jar picker
PICKER_ADDRESS = "jar-picker-address"

download_tmp_dir = None
debug = None
dataDir = None
groupIdFile = None

config = vertx.config()
if not config is None:
  download_tmp_dir = config.get('tmpDir', None)
  dataDir = config.get('dataDir', None)
  debug = config.get('debug', None)
  groupIdFile = config.get('groupIdFile', None)

#
# message handler
# message format is JSON
# 
#
def msg_handler(message):
  body = message.body
  picker = Picker()
  if not download_tmp_dir is None: picker.setTmpDir(download_tmp_dir)
  if not dataDir is None: picker.setDataDir(dataDir)
  if not debug is None: picker.setDebug(debug)
  if not groupIdFile is None: picker.setGroupIdFile(groupIdFile)
  name = body.get("name",None)
  version = body.get("version",None)
  milestone = body.get("milestone",None)
  override = body.get('override', False)
  fileName = "%s/%s-%s-%s.json" % (picker.dataDir, name, version, milestone)
  if milestone is None:
    fileName = "%s/%s-%s.json" % (picker.dataDir, name, version)
  if os.path.exists(fileName):
    if override is True or override == "True":
      vertx.logger().info("Will override existed file")
    else:
      vertx.logger().warn("The request product version has been collected already.")
      message.reply("The request product version has been collected already, use 'override = True' request to proceed.")
      return
  try:    
    result = picker.picks(body)
    message.reply(result)
  except Exception, err:
    message.reply("Error:" + err.message)
  
# end of msg_handler

EventBus.register_handler(PICKER_ADDRESS, handler=msg_handler)
