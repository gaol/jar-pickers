#!/usr/bin/python
#
#  This is the main entrance of the jar-pickers application
#
import os
import vertx

# config about listen verticle
listenConfig = None
listenVerticle = "listen.py"
listenCount = 4

# config about http verticle
httpConfig = None
httpVerticle = "http.py"
httpCount = 4

config = vertx.config()
if not config is None:
  listenConfig = config.get('listen', None)
  if not listenConfig is None: listenCount = listenConfig.get('instances', 4)
  httpConfig = config.get('http', None)
  if not httpConfig is None: httpCount = httpConfig.get('instances', 4)


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

vertx.deploy_verticle(listenVerticle, listenConfig, listenCount, handler = deploy_handler)
vertx.deploy_verticle(httpVerticle, httpConfig, httpCount, handler = deploy_handler)

