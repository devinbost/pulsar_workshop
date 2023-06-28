#! /bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#


CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/../../../_bash/utilities.sh"
 DEBUG=true

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
   echo "                      -c <redelivery | dlt>" 
   echo "                      -dlt <dead letter topic name>"
   echo "                      [-na]"
   echo "       -h  : Show usage info"
   echo "       -t  : (Required) The topic name to publish messages to."
   echo "       -n  : (Required) The number of messages to consume."
   echo "       -cc : (Required) 'client.conf' file path."
   echo "       -c  : (Required) The consumer to run (redelivery or dlt)."
   echo "       -dlt  : (Optional) The dead letter topic to publish to.  Required if -c is 'redelivery'."
   echo "       -na : (Optional) Non-Astra Streaming (Astra streaming is the default)."
   echo
}

if [[ $# -eq 0 || $# -gt 10 ]]; then
   usage
   errExit 10 "Incorrect input parameter count!"
fi

astraStreaming=1
while [[ "$#" -gt 0 ]]; do
   case $1 in
      -h)  usage; exit 0      ;;
      -na) astraStreaming=0;  ;;
      -t)  tpName=$2; shift   ;;
      -dlt)  dltName=$2; shift   ;;
      -n)  msgNum=$2; shift   ;;
      -cc) clntConfFile=$2; shift ;;
      -c) consumerType=$2; shift ;;
      *)  errExit 20 "Unknown input parameter passed: $1" ;;
   esac
   shift
done
debugMsg "astraStreaming=${astraStreaming}"
debugMsg "tpName=${tpName}"
debugMsg "msgNum=${msgNum}"
debugMsg "clntConfFile=${clntConfFile}"
debugMsg "consumerType=${consumerType}"
debugMsg "dltName=${dltName}"

if [[ -z "${tpName}" ]]; then
   errExit 30 "Must provide a valid topic name in format \"<tenant>/<namespace>/<topic>\"!"
fi

if ! [[ -f "${clntConfFile}" ]]; then
   errExit 40 "The specified 'client.conf' file is invalid!"
fi

clientAppJar="${SCENARIO_HOMEDIR}/client-app/target/message-redelivery-clientapp-1.0.0.jar"
if ! [[ -f "${clientAppJar}" ]]; then
  errExit 50 "Can't find the client app jar file. Please first build the programs!"
fi

# generate a random alphanumeric string with length 20
randomStr=$(cat /dev/urandom | LC_ALL=C tr -dc 'a-zA-Z0-9' | fold -w 20 | head -n 1)

if [ ${consumerType} == "redelivery" ]; then
   if [[ -z "${dltName}" ]]; then
      errExit 50 "Dead letter topic must be specified if consumer type is 'redelivery'!"
   fi

   javaCmd="java -cp ${clientAppJar} \
      com.example.pulsarworkshop.RedeliveryConsumer \
      -n ${msgNum} -t ${tpName} -c ${clntConfFile} -dlt ${dltName}"

elif [ ${consumerType}=="dlt" ]; then
   javaCmd="java -cp ${clientAppJar} \
      com.example.pulsarworkshop.DeadLetterConsumer \
      -n ${msgNum} -t ${tpName} -c ${clntConfFile}" 
else 
   errExit 50 "Unknown consumer type!"
fi


if [[ ${astraStreaming} -eq 1 ]]; then
  javaCmd="${javaCmd} -a"
fi
debugMsg="javaCmd=${javaCmd}"

eval ${javaCmd}
