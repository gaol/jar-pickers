#!/usr/bin/python
#
#  This is the main entrance of the vertx verticle
#
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
  picker.picks(body)
  
# end of msg_handler

EventBus.register_handler(PICKER_ADDRESS, handler=msg_handler)
