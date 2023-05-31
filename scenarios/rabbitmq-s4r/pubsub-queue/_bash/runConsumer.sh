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
   echo "                      [-na]"
   echo "                      -q <queue_name>"
   echo "                      -n <message_number to consume. -1=forever>"
   echo "                      -cc <client_conf_file>" 
   echo "       -h  : Show usage info"
   echo "       -na : (Optional) Non-Astra Streaming (Astra streaming is the default)."
   echo "       -q  : (Required) The RabbitMQ queue name to reeive messages from."
   echo "       -n  : (Required) The number of messages to consume. -1=forever"
   echo "       -cc : (Required) RabbitMQ 'client.conf' file and path."
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
      -q)  tpName=$2; shift   ;;
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
   errExit 30 "Must provided a valid queue name in format \"<tenant>/<namespace>/<topic>\"!"
fi

if ! [[ -f "${clntConfFile}" ]]; then
   errExit 40 "The specified client config file is invalid!"
fi

clientAppJar="${SCENARIO_HOMEDIR}/target/s4r-pubsub-queue-1.0.0.jar"
if ! [[ -f "${clientAppJar}" ]]; then
  errExit 50 "Can't find the client app jar file. Please first build the programs!"
fi

javaCmd="java -cp ${clientAppJar} \
    com.example.pulsarworkshop.S4RQueueConsumer \
    -n ${msgNum} -q ${tpName} -c ${clntConfFile} -a ${astraStreaming}"
debugMsg="javaCmd=${javaCmd}"

eval ${javaCmd}