#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/../../../_bash/utilities.sh"
# DEBUG=true

echo

##
# Show usage info 
#
usage() {   
   echo
   echo "Usage: runConsumer.sh [-h]" 
   echo "                      -t <topic_name>"
   echo "                      -n <message_number>"
   echo "                      -cc <client_conf_file>" 
   echo "                      [-na]"
   echo "       -h  : Show usage info"
   echo "       -t  : (Required) The topic name to publish messages to."
   echo "       -n  : (Required) The number of messages to consume."
   echo "       -cc : (Required) 'client.conf' file path."
   echo "       -na : (Optional) Non-Astra Streaming (Astra streaming is the default)."
   echo
}

if [[ $# -eq 0 || $# -gt 8 ]]; then
   usage
   errExit 10 "Incorrect input parametere count!"
fi

astraStreaming=1
while [[ "$#" -gt 0 ]]; do
   case $1 in
      -h)  usage; exit 0      ;;
      -na) astraStreaming=0;  ;;
      -t)  tpName=$2; shift   ;;
      -n)  msgNum=$2; shift   ;;
      -cc) clntConfFile=$2; shift ;;
      *)  errExit 20 "Unknown input parameter passed: $1" ;;
   esac
   shift
done
debugMsg "astraStreaming=${astraStreaming}"
debugMsg "tpName=${tpName}"
debugMsg "msgNum=${msgNum}"
debugMsg "clntConfFile=${clntConfFile}"

if [[ -z "${tpName}" ]]; then
   errExit 30 "Must provided a valid topic name in format \"<tenant>/<namespace>/<topic>\"!"
fi

if ! [[ -f "${clntConfFile}" ]]; then
   errExit 40 "The specified 'client.conf' file is invalid!"
fi

clientAppJar="${SCENARIO_HOMEDIR}/client-app/target/np-msgenrich-avro-clientapp-1.0.0.jar"
if ! [[ -f "${clientAppJar}" ]]; then
  errExit 50 "Can't find the client app jar file. Please first build the programs!"
fi

# generate a random alphanumeric string with length 20
randomStr=$(cat /dev/urandom | LC_ALL=C tr -dc 'a-zA-Z0-9' | fold -w 20 | head -n 1)

javaCmd="java -cp ${clientAppJar} \
    com.example.pulsarworkshop.IoTSensorConsumerAvro \
    -n ${msgNum} -t ${tpName} -c ${clntConfFile} -sbn mysub-${randomStr}"
if [[ ${astraStreaming} -eq 1 ]]; then
  javaCmd="${javaCmd} -a"
fi
debugMsg="javaCmd=${javaCmd}"

eval ${javaCmd}
