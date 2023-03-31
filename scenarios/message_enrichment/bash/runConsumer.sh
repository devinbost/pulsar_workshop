#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/../bash/utilities.sh"

echo

##
# Show usage info 
#
usage() {   
   echo
   echo "Usage: runConsumer.sh [-h]" 
   echo "                      [-na]"
   echo "                      -n <message_number>"
   echo "                      -cc <client_conf_file>" 
   echo "       -h  : Show usage info"
   echo "       -na : (Optional) Non-Astra Streaming (Astra streaming is the default)."
   echo "       -n  : (Required) The number of message to consume."
   echo "       -cc : (Required) 'client.conf' file path."
   echo
}

if [[ $# -eq 0 || $# -gt 4=5 ]]; then
   usage
   errExit 10 "Incorrect input parametere count!"
fi

astraStreaming=1
while [[ "$#" -gt 0 ]]; do
   case $1 in
      -h)  usage; exit 0      ;;
      -na) astraStreaming=0;  ;;
      -n)  msgNum=$2; shift   ;;
      -cc) clntConfFile=$2; shift ;;
      *)  errExit 20 "Unknown input parameter passed: $1" ;;
   esac
   shift
done
debugMsg "astraStreaming=${astraStreaming}"
debugMsg "msgNum=${msgNum}"
debugMsg "clntConfFile=${clntConfFile}"

if ! [[ -f "${clntConfFile}" ]]; then
   errExit 30 "The specified 'client.conf' file is invalid!"
fi

clientAppJar="${SCENARIO_HOMEDIR}/source_code/client_app/target/msgenrich-clientapp-1.0.0.jar"
if [[ -z "${clientAppJar}" ]]; then
  errExit 40 "Can't find the client app jar file. Please run 'deploy.sh -buildApp' to build it!"
fi

if [[ ${astraStreaming} -eq 1 ]]; then
  java \
    -cp ${clientAppJar}  \
    com.example.pulsarworkshop.IoTSensorConsumer \
    -a -n ${msgNum} -t ymtest/default/t2 -c ${clientConfFile}  -sbn mysub
else
  java \
      -cp ${clientAppJar}  \
      com.example.pulsarworkshop.IoTSensorConsumer \
      -n ${msgNum} -t ymtest/default/t2 -c ${clientConfFile}  -sbn mysub
fi
