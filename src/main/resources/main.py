#!/usr/bin/python
#
#  This is the main entrance of the jar-pickers application
#
import os
VERTX = None
try:
  import vertx
  VERTX = True
except:
  pass
class DEFAULT():
  """
  Default configurations
  """
  DOWNLOAD_TMP_DIR = "%s/tmp" % os.getcwd()
  HTML_DIR = "%s/html" % os.getcwd()
  DATA_DIR = "%s/data/products" % os.getcwd()
  GROUPID_FILE = "%s/groupids.ini" % DATA_DIR
  PRODUCTS_FILE = "%s/products.ini" % DATA_DIR
  DEBUG = False
  LISTENCOUNT = 4
  HTTPCOUNT = 4

  # http default configurations
  DEFAULT_HOST = "localhost"
  DEFAULT_PORT = 8090

  # default listening address 
  PICKER_ADDRESS = "jar-picker-address"
  PICKER_REPLY_ADDRESS = "jar-picker-reply-address"

  
#end of class DEFAULT

# common configuration like data directory, temporary download directory, etc.
commonConfig = None

# config about listen verticle
listenConfig = None
listenVerticle = "listen.py"
listenCount = DEFAULT.LISTENCOUNT

# config about http verticle
httpConfig = None
httpVerticle = "http.py"
httpCount = DEFAULT.HTTPCOUNT

if not VERTX is None:
  config = vertx.config()
  if not config is None:
    commonConfig = config.get('common', None)
    listenConfig = config.get('listen', None)
    if not listenConfig is None: 
      listenConfig['common'] = commonConfig
      listenCount = listenConfig.get('instances', DEFAULT.LISTENCOUNT)
    httpConfig = config.get('http', None)
    if not httpConfig is None:
      httpConfig['common'] = commonConfig
      httpCount = httpConfig.get('instances', DEFAULT.HTTPCOUNT)

def deploy_handler(err, deployment_id):
  """
  Deploy handler on whether the verticles are deployed successfully.
  """
  if err is not None:
    raise err
  else:
    print "Deployment succeefully. %s" % deployment_id
#end of deploy_handler

# starts up deploy other verticles within the same module

if not VERTX is None:
  vertx.deploy_verticle(listenVerticle, listenConfig, listenCount, handler = deploy_handler)
  vertx.deploy_verticle(httpVerticle, httpConfig, httpCount, handler = deploy_handler)

