#!/bin/sh

URLBASE="http://10.66.78.40:8080/trackers/api"
BREWWEB="https://brewweb.devel.redhat.com/search?match=glob&type=maven&terms="

artisRedHat="artis_redhat.list"
curl -s "${URLBASE}/a/all" | grep "\-redhat\-" > ${artisRedHat}

for arti in `cat ${artisRedHat}`;
do 
  gid=`echo -e $arti|cut -d ":" -f1`
  artiId=`echo -e "$arti" |cut -d ":" -f2`
  ver=`echo -e $arti|cut -d ":" -f3`
  t=`echo -e $arti|cut -d ":" -f4`
  if [ "${t}" == "maven-plugin" -o "${t}" == "bundle" ]; then
    t="jar"
  fi
  buildInfo=`echo -e $arti|cut -d ":" -f5`
  if [ "${buildInfo}" != "" ]; then
    continue
  fi
  artiCheck=`echo -e $arti|cut -d ":" -f7`
  if [ "${artiCheck}" == "" ]; then
    continue
  fi
  # search the jar or pom file from brewweb
  artiFile="$artiId-$ver.${t}"
  if [ "${t}" != "jar" -a "${t}" != "pom" ]; then
    artiFile="$artiId-$ver-$t.jar"
  fi
  echo -e "Searching $artiFile from $BREWWEB${artiFile}"
  archiveInfo=`curl -s -k -i "$BREWWEB${artiFile}"`
  if [ "$(echo -e "$archiveInfo" |grep -E "^HTTP\/1\.1 302" )" != "" ]; then
    archiveLink=`echo -e "$archiveInfo" | grep "^Location:" | cut -d ":" -f2 | sed "s/ //g"`
    echo -e "archiveLink: https://brewweb.devel.redhat.com/$archiveLink"
    buildLine=`curl -s -k "https://brewweb.devel.redhat.com/${archiveLink}" | grep -E "href=\"buildinfo\?buildID="|sed "s/<a/\n<a/" |sed "s/<\/a>/<\/a>\n/"|grep "<a href="`
    link=`echo -e ${buildLine} | cut -d "\"" -f2`
    buildID=`echo -e "${buildLine}" | cut -d ">" -f2 | cut -d "<" -f1`
    buildInfoUpdate="[${buildID}](https://brewweb.devel.redhat.com/${link})"
    echo -e "curl -H \"Authorization: Basic dHJhY2tlcjp0cmFja2VyMSNwd2Q=\" -X POST -d "\"buildInfo=${buildInfoUpdate}\"" \"${URLBASE}/ab/$gid:$artiId:$ver\""
  elif [ "$(echo -e "$archiveInfo" |grep -E "^HTTP\/1\.1 200" )" != "" ]; then
    # grep "a href=\"archiveinfo" |sed "s/<a href/\n<a href/g" | sed "s/<\/a>/<\/a>\n/g" | grep "<a href" 
    for aL in `echo -e "${archiveInfo}" | grep "a href=\"archiveinfo" |sed "s/<a href/\n<a href/g" | sed "s/<\/a>/<\/a>\n/g" | grep "<a href" | sed "s/ //g"`;
    do
     aLL=`echo -e "${aL}" |cut -d "\"" -f2`
     #cadidateLink=`curl -s -k "https://brewweb.devel.redhat.com/${aLL}"` 
     echo -e "candidateLink: https://brewweb.devel.redhat.com/${aLL}"
     check=`curl -s -k "https://brewweb.devel.redhat.com/${aLL}" | grep "Checksum" | cut -d ">" -f4 | cut -d "<" -f1`
     echo -e "checksum: $check, the arti check is: $artiCheck"
     if [ "$check" == "$artiCheck" ]; then
       buildLine=`curl -s -k "https://brewweb.devel.redhat.com/${aLL}" | grep "href=\"buildinfo?buildID="|sed "s/<a/\n<a/" |sed "s/<\/a>/<\/a>\n/"|grep "<a href=" |sed "s/ //g"`
       link=`echo -e ${buildLine} | cut -d "\"" -f2`
       buildID=`echo -e "${buildLine}" | cut -d ">" -f2 | cut -d "<" -f1`
       buildInfoUpdate="[${buildID}](https://brewweb.devel.redhat.com/${link})"
       echo -e "curl -H \"Authorization: Basic dHJhY2tlcjp0cmFja2VyMSNwd2Q=\" -X POST -d "buildInfo=${buildInfoUpdate}" \"${URLBASE}/ab/$gid:$artiId:$ver\""
       break;
     fi
    done
  fi
done
