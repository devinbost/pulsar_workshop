#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/../../_bash/utilities.sh"
# DEBUG=true

echo

##
# Show usage info 
#
usage() {   
   echo
   echo "Usage: runProducer.sh [-h]" 
   echo "                      -t <topic_name>"
   echo "                      -n <message_number>"
   echo "                      -cc <client_conf_file>" 
   echo "                      [-na]"
   echo "       -h  : Show usage info"
   echo "       -t  : (Required) The topic name to publish messages to."
   echo "       -n  : (Required) The number of messages to produce."
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
brkrServiceUrl=$(getPropVal ${clntConfFile} "brokerServiceUrl")
webServiceUrl=$(getPropVal ${clntConfFile} "webServiceUrl")
authPlugin=$(getPropVal ${clntConfFile} "authPlugin")
authParams=$(getPropVal ${clntConfFile} "authParams")
debugMsg "brkrServiceUrl=${brkrServiceUrl}"
debugMsg "webServiceUrl=${webServiceUrl}"
debugMsg "authPlugin=${authPlugin}"
debugMsg "authParams=${authParams}"

if ! [[ -z "${authPlugin}" ]]; then
    jwtTokenAuthEnabled=1
    IFS=':' read -r -a tokenStrArr <<< "${authParams}"
    jwtTokenValue="${tokenStrArr[1]}"
    debugMsg "jwtTokenValue=${jwtTokenValue}"
fi
if [[ ${jwtTokenAuthEnabled} -eq 1 && -z "${jwtTokenValue// }" ]]; then
    errExit 50 "Missing JWT token value in the specified 'client.conf' file when JWT token authentication is enabled!"
fi


producerSubFolder="spb-pulsar-producer"

clientAppJar="${SCENARIO_HOMEDIR}/${producerSubFolder}/target/spb-pulsar-producer-1.0.0.jar"
if ! [[ -f "${clientAppJar}" ]]; then
  errExit 60 "Can't find the client app jar file. Please first build the programs!"
fi

iotDataSrcFile="${SCENARIO_HOMEDIR}/../../_raw_data_src/sensor_telemetry.csv"
if ! [[ -f "${iotDataSrcFile}" ]]; then
  errExit 7 0 "Can't find the IoT sensor data source file is invalid!"
fi


#####
## 1. Generate "application.yml" file that is required by the Spring Boot application
#####
springConfigYamlTemplate="${SCENARIO_HOMEDIR}/../_templates/producer.application.yml.tmpl"
if ! [[ -f "${springConfigYamlTemplate}" ]]; then
    errExit 100 "The specified producer spring application config template yaml file is invalid!"
fi

producerConfigYaml="${SCENARIO_HOMEDIR}/${producerSubFolder}/application.yml"
cp -rf ${springConfigYamlTemplate} ${producerConfigYaml}

replaceStringInFile "<TMPL_pulsar_broker_service_url>" "${brkrServiceUrl}" "${producerConfigYaml}"
replaceStringInFile "<TMPL_pulsar_web_service_url>" "${webServiceUrl}" "${producerConfigYaml}"
if [[ ${jwtTokenAuthEnabled} -eq 1 ]]; then
    replaceStringInFile \
        "<TMPL_auth_method>" \
        "org.apache.pulsar.client.impl.auth.AuthenticationToken" \
        "${producerConfigYaml}"
    replaceStringInFile \
        "<TMPL_auth_parameters>" \
        "token: ${jwtTokenValue}" \
        "${producerConfigYaml}"
else
    replaceStringInFile "<TMPL_auth_method>" "" "${producerConfigYaml}"
    replaceStringInFile "<TMPL_auth_parameters>" "" "${producerConfigYaml}"
fi

replaceStringInFile "<TMPL_num_message>" "${msgNum}" "${producerConfigYaml}"
replaceStringInFile "<TMPL_full_topic_name>" "${tpName}" "${producerConfigYaml}"
replaceStringInFile "<TMPL_iot_source_csv_file>" "${iotDataSrcFile}" "${producerConfigYaml}"

#####
## 2. Run the producer Spring Boot application
#####
javaCmd="java \
    -cp ${clientAppJar} \
    com.example.pulsarworkshop.IoTSensorSpbpProducer \
    --spring.config.location=${SCENARIO_HOMEDIR}/${producerSubFolder}/"
debugMsg "javaCmd=${javaCmd}"

eval ${javaCmd}