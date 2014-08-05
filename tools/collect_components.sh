#!/bin/bash

baseURL="http://buildtracker-stg.jboss.org/build-tracker"
startURL="http://buildtracker-stg.jboss.org/build-tracker/projects-list"
for projectline in `curl -s "${startURL}" | grep -A1 "href=\"\.\/project"| grep -v "<div" | grep -E "\?id=|<span>" |sed "s/ //g" | tr "\n" " " |sed "s/<\/span>/<\/span>\n/g" |sed "s/ //g" |sed "s/\.//g"`;
do
  projectlink=`echo -e "${projectline}" |cut -d "\"" -f2`;
  pl="${baseURL}${projectlink}";
  projectname=`echo -e ${projectline} |cut -d ">" -f3 | cut -d "<" -f1`
  echo -e "Found: ${projectname}"  

  for release in `curl -s -L ${pl}| grep "href=\"\.\/release"|sed "s/ //g" | cut -d "\"" -f2 | sed "s/\.//"`;
  do
      releaselink="${baseURL}${release}"
      artisLine=`curl -s -L ${releaselink} | grep "tabs-tab-4-tabLink" |grep "href"| cut -d "\"" -f4|sed "s/\.//"`
      artisLink="${baseURL}${artisLine}"
      for artiL in `curl -s -L ${artisLink} |grep "ArtifactPage" |sed "s/ //g" | sed "s/\.//"`;
      do
        grpIdLine=`echo -e ${artiL} | cut -d "\"" -f2`
        releaseVer=`echo -e ${artiL} |cut -d ">" -f4|cut -d "<" -f1`
        grpIdLink="${baseURL}${grpIdLine}"
        grpLine=`curl -s -L ${grpIdLink} |grep -A5 "<h1>"|grep "<span"|tr "\n" " "|sed "s/ //g"`
        grpId=`echo -e "${grpLine}" |cut -d ">" -f2 |cut -d "<" -f1`
        componentline="${projectname}:${releaseVer}:${grpId}"
        echo -e "\t ${componentline}" |tee -a components.list
      done
  done
done
