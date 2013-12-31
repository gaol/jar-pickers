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
htmlDir = DEFAULT.HTML_DIR

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
    htmlDir = commonConfig.get('htmlDir', DEFAULT.HTML_DIR)

if not port is None:
  port = int(port)


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
    request.response.end("Bad Request: name must be provided")
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
  request.response.end(json.dumps(products, indent = 2))
  
#end of queryJar


def queryProduct(request):
  """
  Search which artifacts one product version has.
  """
  name = request.params.get("name", None)
  version = request.params.get("version", None)
  milestone = request.params.get("milestone", None)
  if name is None or version is None:
    request.response.status_code = 404
    request.response.status_message = "Bad Request"
    request.response.end("Bad Request: name and version must be provided")
    return
  fileName = "%s/%s-%s.json" % (dataDir, name.upper(), version)
  if milestone is not None and (not milestone in ['','null','None','undefined']): fileName = "%s/%s-%s-%s.json" % (dataDir, name.upper(), version, milestone)
  artifacts = []
  if not os.path.exists(fileName):
    request.response.status_code = 400
    request.response.status_message = "Not Found"
    request.response.end("Not Found: File: %s does not exist." % fileName)
    return
  data = json.load(file(fileName))
  for arti in data['artifacts']:
    artiKey = {}
    artiKey['type'] = arti.split(":")[0]
    if arti.split(":")[0] == 'jar':
      artiKey['name'] = arti.split(":")[2]
      artiKey['version'] = arti.split(":")[3]
    elif arti.split(":")[0] == 'package':
      artiKey['name'] = arti.split(":")[1]
      artiKey['version'] = arti.split(":")[2]
    if not artiKey in artifacts: artifacts.append(artiKey)
  
  request.response.put_header('Content-Type', 'application/json')
  request.response.status_code = 200
  request.response.status_message = "OK"
  request.response.end(json.dumps(sorted(artifacts), indent = 2))

#end of queryProduct

def queryProductList(request):
  """
  Write products list back to client in json format
  """
  products = []
  for name, fullName in picker.getProducts():
    products.append({"name" : name, "fullname" : fullName})
  request.response.put_header('Content-Type', 'application/json')
  request.response.status_code = 200
  request.response.status_message = "OK"
  request.response.end(json.dumps(sorted(products), indent = 2))

#end of queryProductList

def queryProductVersions(request):
  """
  Write available versions of a product in json format. a list
  """
  name = request.params.get("name", None)
  if name is None:
    request.response.status_code = 404
    request.response.status_message = "Bad Request"
    request.response.end("Bad Request: name must be provided")
    return

  versions = []
  jsons = getJsonDataList(dataDir)
  products = []
  for jsonFile in jsons:
    data = json.load(file(jsonFile))
    version = {"version" : data['version']}
    if name.upper() == data['name'] and (not version in versions):
      versions.append(version)
  request.response.put_header('Content-Type', 'application/json')
  request.response.status_code = 200
  request.response.status_message = "OK"
  request.response.end(json.dumps(sorted(versions), indent = 2))
#end of queryProductVersions

def queryProductMilestones(request):
  """
  Write available milestones of a product/version in json format. a list
  """
  name = request.params.get("name", None)
  version = request.params.get("version", None)
  if name is None:
    request.response.status_code = 404
    request.response.status_message = "Bad Request"
    request.response.end("Bad Request: name must be provided")
    return

  milestones = []
  jsons = getJsonDataList(dataDir)
  products = []
  for jsonFile in jsons:
    data = json.load(file(jsonFile))
    milestone = {"milestone" : data['milestone']}
    if name.upper() == data['name'] and (version is None or version == data['version']):
      milestones.append(milestone)
  request.response.put_header('Content-Type', 'application/json')
  request.response.status_code = 200
  request.response.status_message = "OK"
  request.response.end(json.dumps(sorted(milestones), indent = 2))
#end of queryProductMilestones


def sendFileBack(request):
  """
  Send file back
  """
  fileName = request.path[1:]
  if fileName == '': fileName = "html/index.html"
  if fileName in ['html/','html']: fileName = "html/index.html"
  request.response.send_file(fileName)
#end of sendFileBack

server = vertx.create_http_server()

@server.request_handler
def request_handler(request):
  try:
    if request.path.endswith('/picker.do'):
      pickerRequest(request)
    elif request.path.endswith('/jar.do'):
      queryJar(request)
    elif request.path.endswith('/product.do'):
      queryProduct(request)
    elif request.path.endswith('/products.do'):
      queryProductList(request)
    elif request.path.endswith('/versions.do'):
      queryProductVersions(request)
    elif request.path.endswith('/milestones.do'):
      queryProductMilestones(request)
    elif request.path.startswith('/html/') or request.path in ['','/']:
      sendFileBack(request)
    else:
      printRequestInfo(request)
  except Throwable, err:
    vertx.logger().error("Error when handling the request.", err) 
    request.response.status_code = 500
    request.response.status_message = "Inner Error"
    request.response.end()
    request.response.close()
#end of request_handler



def listen_handler(err, server):
  """
  Determine whether the http server is started or not.
  """
  if err is not None:
    print "Oops, Error when starts the server"
    raise err

server.listen(port, host, listen_handler)
