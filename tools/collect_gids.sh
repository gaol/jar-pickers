#!/bin/bash

scriptbasename=$(basename $0)
cwd=$(dirname $0)

die() {
    if ! [ -z "$*" ]
    then
        echo "$scriptbasename Error: $@" >&2
    fi
    exit 1
}

Hello() {
cat << EOF
  This script is used to collect groupIds from public maven repository
  The default maven repository used here is: http://repository.jboss.org/nexus/content/groups/public/org/jboss/

  Or you you can specify the maven repository URL by '-u URL'

  The output file will be called: 'groupids_collected.ini' in current directory

EOF
}

Hello

# Detect which version of getopt we have
getopt -T >/dev/null 2>/dev/null || [ "$?" -eq 4 ] || die "Need enhanced getopt from util-linux to parse options"

usage() { echo "Usage: $0 [-u <url>] [-f <filter>] [-o <output>]" 1>&2; exit 1; }

url="http://repository.jboss.org/nexus/content/groups/public/org/jboss/"

output="$cwd/groupids_collected.ini"
filter=""

while getopts ":u:f:o:" o; do
    case "${o}" in
        u)
            url=${OPTARG}
            ;;
        f)
            filter=${OPTARG}
            ;;
        o)
            output=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))


# write '[groupids]' to output file firsth
echo -e "[groupids]" > $output

# getGroupIds baseurl
# just print each line with format: artifactId = groupId
getGroupIds() {
  local u="$1"
  echo -e "Checking url: ${u}"
  if [ "${filter}" != "" ]; then
    if [ $(echo "${u}" |grep "${filter}") ]; then
      echo -e "Filter out: ${u}"
      return
    fi
  fi
  hrefs=$(curl -s "${u}"|grep "<a href=\"" |grep "${u}" | cut -d "\"" -f 2)

  mvn_meta_xml=$(echo -e "${hrefs}" | grep -E "maven-metadata.xml$")
  echo -e "mvn meta xml is: $mvn_meta_xml"

  groupId=""
  artifactId=""
  if [ $mvn_meta_xml ];
  then
    # has mvn_meta_xml
    groupId=$(curl -s "${mvn_meta_xml}"|grep "<groupId>" | cut -d ">" -f 2| cut -d "<" -f1)
    artifactId=$(curl -s "${mvn_meta_xml}"|grep "<artifactId>" | cut -d ">" -f 2| cut -d "<" -f1)
  fi
  
  if [ "$groupId" != "" ] && [ "$artifactId" != "" ]; then
    # OK, print it out, then return
    echo -e "$artifactId=$groupId" >> $output
    return
  fi
  if [ "${groupId}" == "" ] || [ "${artifactId}" == "" ]; then
    # needs to check sub directories
    echo -e "start to check sub directories"
    for href in $hrefs; do
      if [ $(echo -e "$href" | grep -E "\/$") ]; then
        getGroupIds $href
      fi
    done
  fi
}

getGroupIds ${url}

