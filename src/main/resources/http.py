#!/usr/bin/python
#
#  This is the http for the jar-pickers application.
#  It will be deployed by the main verticle.
#
import os
import vertx
from core.event_bus import EventBus
from java.lang import Throwable
from main import DEFAULT

host = DEFAULT.DEFAULT_HOST
port = DEFAULT.DEFAULT_PORT

download_tmp_dir = None
debug = None
dataDir = None
groupIdFile = None

config = vertx.config()
if not config is None:
  host = config.get('host', DEFAULT.DEFAULT_HOST)
  port = config.get('port', DEFAULT.DEFAULT_PORT)
  commonConfig = config.get('common', None)
    if not commonConifg is None:
      download_tmp_dir = commonConfig.get('tmpDir', DEFAULT.DOWNLOAD_TMP_DIR)
      dataDir = commonConfig.get('dataDir', DEFAULT.DATA_DIR)
      debug = commonConfig.get('debug', DEFAULT.DEBUG)
      groupIdFile = commonConfig.get('groupIdFile', DEFAULT.GROUPID_FILE)

def reply_handler(message):
  EventBus.send(DEFAULT.PICKER_REPLY_ADDRESS, message.body)
  if not debug is None: print message.body
    
#end of reply_handler

def pickerRequest(request):
  """
  Function to handle the request to picks up the jars 
  """
  name = request.params.get("name", None)
  version = request.params.get("version", None)
  milestone = request.params.get("milestone", None)
  urls = request.params.get_all("urls")

  try:
    request.response.put_header('Content-Type', 'text/plain')
    message = {"name" : name, "version" : version, "milestone" : milestone, "urls" : urls}
    EventBus.send(DEFAULT.PICKER_ADDRESS, message,reply_handler)
    request.response.write_str("Request Submitted.")
    request.response.status_code = 200
    request.response.status_message = "OK"
  except Throwable, err:
    request.response.write_str("Failed to submit the request.")
    request.response.status_code = 500
    request.response.status_message = err.message
  finally:
    request.response.end()
#end of pickerRequest

def printRequestInfo(request, end=True):
  request.response.put_header('Content-Type', 'text/plain')
  str = "Headers are\n"
  for key in request.headers.keys():
    for value in request.headers.get_all(key):
      str += "\t%s: %s\n" % (key, value)

  str += "\nParameters are\n"
  for key in request.params.keys():
    for value in request.params.get_all(key):
      str += "\t%s: %s\n" % (key, value)

  if end is True: request.response.end(str)
#end of printRequestInfo


def queryJar(request):
  """
  Search which product versions does the request jar belongs to
  """
  request.response.put_header('Content-Type', 'application/json')
  name = request.params.get('name', None)
  version = request.params.get('version', None)
  if name is None:
    request.response.status_code = 404
    request.response.status_message = "Bad Request"
    request.response.end("Bad Request")
    return
  
  
  
#end of queryJar

server = vertx.create_http_server()

@server.request_handler
def request_handler(request):
  try:
    if request.path.endswith('/picker.do'):
      pickerRequest(request)
    elif request.path.endswith('/jar.do'):
      queryJar(request)
    else:
      printRequestInfo(request)
  except Throwable, err:
    vertx.logger().error("Error when handling the request.", err) 
    request.response.status_code = 500
    request.response.status_message = "Inner Error"
    request.response.end()
#end of request_handler



def listen_handler(err, server):
  """
  Determine whether the http server is started or not.
  """
  if err is not None:
    print "Oops, Error when starts the server"
    raise err

server.listen(port, host, listen_handler)
