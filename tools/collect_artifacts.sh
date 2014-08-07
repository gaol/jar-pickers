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
  This script is used to collect artifacts from maven repository
  The default maven repository used here is: http://repository.jboss.org/nexus/content/groups/public/org/jboss/

  Or you you can specify the maven repository URL by '-u URL'

  The output file will be called: 'artifacts_collected.list' in current directory by default
 
  The format of the output file: 'groupId:artifactId:version[:type]'

EOF
}

Hello

# Detect which version of getopt we have
getopt -T >/dev/null 2>/dev/null || [ "$?" -eq 4 ] || die "Need enhanced getopt from util-linux to parse options"

usage() { echo "Usage: $0 [-u <url>] [-f <filter>] [-o <output>]" 1>&2; exit 1; }

url="http://repository.jboss.org/nexus/content/groups/public/org/jboss/"

output="$cwd/artifacts_collected.list"
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

echo -e "Base URL is:$url"
echo -e "Output file is:$output"
echo -e "Filter is:$filter"

# getArtifacts baseurl
# just print each line with format: artifactId = groupId
getArtifacts() {
  local u="$1"
  echo -e "Checking url: ${u}"
  if [ "${filter}" != "" ]; then
    if [ $(echo "${u}" |grep "${filter}") ]; then
      echo -e "Filter out: ${u}"
      return
    fi
  fi
  hrefs=$(curl -s "${u}" |grep "<a href="|sed "s/<a/\n<a/g"|grep -E "<a href=" | cut -d "\"" -f2|grep -E "\/$|maven-metadata.xml$" | grep -v "^\/" | grep -v "\.\.")
  hrefMs=()
  for href in ${hrefs[@]}; do
    if [ "$(echo -e $href | grep -v -E '^http' | grep -E '^[^\/]')" != "" ]; then
      hrefM="${u}${href}"
      hrefMs+=("${hrefM}")
    elif [ "$(echo -e $href|grep ${u})" != "" ]; then
      hrefM="$href"
      hrefMs+=("${hrefM}")
    fi
  done

  mvn_meta_xml=$(echo -e "${hrefMs[@]}" | sed "s/ /\n/g" | grep -E "maven-metadata.xml$")

  local groupId=""
  local artifactId=""
  if [ "$mvn_meta_xml" != "" ];
  then
    # has mvn_meta_xml
    groupId=$(curl -s "${mvn_meta_xml}"|grep "<groupId>" | cut -d ">" -f 2| cut -d "<" -f1)
    artifactId=$(curl -s "${mvn_meta_xml}"|grep "<artifactId>" | cut -d ">" -f 2| cut -d "<" -f1)
  fi
  
  if [ "$groupId" != "" ] && [ "$artifactId" != "" ]; then
    # OK, get the versions and print it out, then return
    for ver in $(curl -s "${mvn_meta_xml}" | grep "<version>" | cut -d ">" -f2 |cut -d "<" -f1);
    do
      grpURL=$(echo -e ${groupId} |sed "s/\./\//g")
      pomURL="${url}/${grpURL}/${artifactId}/${ver}/${artifactId}-${ver}.pom"
      local tp=$(curl -s ${pomURL} |grep "<packaging>" | cut -d ">" -f2 | cut -d "<" -f1)
      if [ "${tp}" == "" ]; then
        tp="jar"
      fi
      echo -e "${groupId}:${artifactId}:${ver}:${tp}" | tee -a $output
    done
    return
  fi
  if [ "${groupId}" == "" ] || [ "${artifactId}" == "" ]; then
    # needs to check sub directories
    echo -e "start to check sub directories"
    for href in ${hrefMs[@]}; do
      if [ $(echo -e "$href" | grep -E "\/$") ]; then
        getArtifacts $href
      fi
    done
  fi
}

getArtifacts ${url}

