#!/bin/bash
cli_jar=`find . -name "irc-bot-*-cli.jar" -a -type f`
if [ "${cli_jar}" != "" ]; then
  java -jar ${cli_jar} "$@"
fi
