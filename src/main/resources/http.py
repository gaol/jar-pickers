#!/usr/bin/python
#
#  This is the http for the jar-pickers application.
#  It will be deployed by the main verticle.
#
import os
import json

import vertx
from core.event_bus import EventBus
from java.lang import Throwable

from main import DEFAULT
import picker

host = DEFAULT.DEFAULT_HOST
port = DEFAULT.DEFAULT_PORT

download_tmp_dir = DEFAULT.DOWNLOAD_TMP_DIR
debug = DEFAULT.DEBUG
dataDir = DEFAULT.DATA_DIR
groupIdFile = DEFAULT.GROUPID_FILE

config = vertx.config()
if not config is None:
  host = config.get('host', DEFAULT.DEFAULT_HOST)
  port = config.get('port', DEFAULT.DEFAULT_PORT)
  commonConfig = config.get('common', None)
  if not commonConfig is None:
    download_tmp_dir = commonConfig.get('tmpDir', DEFAULT.DOWNLOAD_TMP_DIR)
    dataDir = commonConfig.get('dataDir', DEFAULT.DATA_DIR)
    debug = commonConfig.get('debug', DEFAULT.DEBUG)
    groupIdFile = commonConfig.get('groupIdFile', DEFAULT.GROUPID_FILE)


def getJsonDataList(dir):
  jsons = []
  for root, dirs, files in os.walk(dir):
    for file in files:
      if file.endswith(".json"):
        jsons.append(os.path.join(root, file))
  return jsons
#end of getJsonDataList


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
    request.response.status_code = 200
    request.response.status_message = "OK"
    request.response.end("Request Submitted.")
  except Throwable, err:
    request.response.status_code = 500
    request.response.status_message = err.message
    request.response.end("Failed to submit the request.")
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
  name = request.params.get('name', None)
  version = request.params.get('version', None)
  if name is None:
    request.response.status_code = 404
    request.response.status_message = "Bad Request"
    request.response.end("Bad Request")
    return
  searchKey = ":%s:" % name
  if not version is None: searcykey = ":%s:%s" % (name, version)
  jsons = getJsonDataList(dataDir)
  products = []
  for jsonFile in jsons:
    data = json.load(file(jsonFile))
    artifacts = data['artifacts']
    for arti in artifacts:
      if searchKey in arti:
        product = {"jar" : arti, "name" : data['name'], "full-name" : picker.getProductFullName(data['name'].upper()), "version" : data['version'], "milestone" : data['milestone'], "urls" : data['urls']}
        products.append(product)
        break

  request.response.put_header('Content-Type', 'application/json')
  request.response.status_code = 200
  request.response.status_message = "OK"
  request.response.end(str(products))
  
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
