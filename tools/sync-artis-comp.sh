#!/bin/sh

URLBASE="http://10.66.78.40:8080/trackers/api"

artisRedHat="artis_redhat.list" # version with -redhat-X
curl -s "${URLBASE}/a/all" | grep "\-redhat\-" > ${artisRedHat}
echo -e "Collected all -redhat-X artifacts without components associated to file: ${artisRedHat}"

compsRedHat="compos.list"
curl -s "${URLBASE}/c/all" | sort -u > ${compsRedHat}
echo -e "Collected all components to file: ${compsRedHat}"

for arti in `cat ${artisRedHat}`;
do 
  gid=`echo -e $arti|cut -d ":" -f1`
  artiId=`echo -e "$arti" |cut -d ":" -f2`
  ver=`echo -e $arti|cut -d ":" -f3`
  compId=`echo -e $arti|cut -d ":" -f6`
  if [ "$compId" != "" ]; then
    continue
  fi
  comps=`cat ${compsRedHat}|grep -v -E "JBossClustering|JBossSSO"|grep ":${ver}:${gid}$"`
  if [ "$comps" == "" ]; then
    verC=`echo -e "$ver"|sed "s/-redhat-[0-9]*//g"`
    comps=`cat ${compsRedHat}|grep -v -E "JBossClustering|JBossSSO" |grep ":${verC}:${gid}$"`
  fi
  if [ "$comps" == "" ]; then
    continue
  fi
  cn=`echo -e "$comps"|cut -d ":" -f1`
  verCP=`echo -e "$comps"|cut -d ":" -f2`
  echo -e "curl -H \"Authorization: Basic dHJhY2tlcjp0cmFja2VyMSNwd2Q=\" -X POST \"${URLBASE}/ac/$gid:$artiId:$ver/$cn:$verCP\""
done
