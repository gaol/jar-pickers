#!/bin/bash
cli_jar=`find . -name "irc-bot-*-cli.jar" -a -type f`
if [ "${cli_jar}" != "" ]; then
  exec "`which java`" -Djava.net.preferIPv4Stack=true  -jar ${cli_jar} "$@"
fi
