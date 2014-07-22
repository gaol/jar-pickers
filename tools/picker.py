#!/usr/bin/python
#
#  This scirpt is used to analyze jar information from a zip file or a directory which contains jar files
#
#  The script will try to find 'pom.properties' from each jar file, then get the groupId,artifactId and version. The default type of the jar is: 'jar', if the jar file name matchees: '{artifactId}-{type}-{version}jar', then the type will be parsed as: {type}.
#
#  It is tested against EAP 6
#

import sys
import os.path

from __init__ import *

def listValues(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))
#end of listValues

def main():
  """
   Main entrance for command line.
  """
  usage="%prog [options]"
  description="""  name, version and urls must be provided. """
  parser = optparse.OptionParser(usage=usage, description = description)
  parser.add_option('-d', '--debug', dest='debug', action='store_true', help='Print debug message', default = False)
  parser.add_option('-n', '--name', dest='name', help='Name of the product, like: eap, ews, etc. This is required')
  parser.add_option('-v', '--version', dest='version', help='Version of the product, like: 6.2.4,6.2.3.ER3, etc. This is required')
  parser.add_option('-u', '--urls', dest='urls', type='string', help='URLs of the product zip file, or local directory. This is required', action='callback', callback=listValues)
  options, args = parser.parse_args()
  name = options.name
  version = options.version
  urls= options.urls
  if name is None or version is None or urls is None:
    parser.print_help()
    sys.exit(1)
  if options.debug:
    log.setDebug()
  else:
    log.noDebug()
  picker = None
  if name.upper() == "EAP" and version.startswith("6"):
    picker = Picker()
  elif name.upper() == "EAP" and version.startswith("5"):
    from eap5 import EAP5Picker
    picker = EAP5Picker()
  else:
    log.warn("unsupported products, try to use the way to analyze EAP 6")
    picker = Picker()
  if not picker is None:
    result = picker.picks({"name" : name, "version" : version, "urls" : urls})
    if len(result) != 0:
      log.error("Errors occur during parsing")
      for er in result:
        if er.startswith("WARN:"):
          log.warn(er)
        if er.startswith("ERROR:"):
          log.error(er)
#end of main

if __name__ == '__main__':
  dirname = os.path.dirname(sys.argv[0])
  if not dirname in sys.path:
    sys.path.append(dirname)
  main()
