#!/bin/python
#
#  This is the main entrance of the vertx verticle
#
import sys
import os
import os.path
import vertx

from core.event_bus import EventBus


# This is the address registred to listen which will trigger the jar picker
PICKER_ADDRESS = "jar-picker-address"

#
# message handler
# message format is JSON
# 
#
def msg_handler(message):
  body = message.body
  print "body is: %s" % body
# end of msg_handler


EventBus.register_handler(PICKER_ADDRESS, handler=msg_handler)
