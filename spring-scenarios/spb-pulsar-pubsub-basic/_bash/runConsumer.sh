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
   echo "Usage: runConsumer.sh [-h]" 
   echo "                      -t <topic_name>"
   echo "                      -n <message_number>"
   echo "                      -cc <client_conf_file>"
   echo "                      [-na]"
   echo "       -h  : Show usage info"
   echo "       -t  : (Required) The topic name to publish messages to."
   echo "       -n  : (Required) The number of messages to consume."
   echo "       -cc : (Required) 'client.conf' file path."
   echo
}

if [[ $# -eq 0 || $# -gt 7 ]]; then
   usage
   errExit 10 "Incorrect input parametere count!"
fi

while [[ "$#" -gt 0 ]]; do
   case $1 in
      -h)  usage; exit 0      ;;
      -t)  tpName=$2; shift   ;;
      -n)  msgNum=$2; shift   ;;
      -cc) clntConfFile=$2; shift ;;
      *)  errExit 20 "Unknown input parameter passed: $1" ;;
   esac
   shift
done
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


consumerSubFolder="spb-pulsar-consumer";

clientAppJar="${SCENARIO_HOMEDIR}/${consumerSubFolder}/target/spb-pulsar-consumer-1.0.0.jar"
if ! [[ -f "${clientAppJar}" ]]; then
  errExit 50 "Can't find the client app jar file. Please first build the programs!"
fi


#####
## 1. Generate "application.yml" file that is required by the Spring Boot application
#####
springConfigYamlTemplate="${SCENARIO_HOMEDIR}/../_templates/consumer.application.yml.tmpl"
if ! [[ -f "${springConfigYamlTemplate}" ]]; then
    errExit 100 "The specified consumer spring application config template yaml file is invalid!"
fi

consumerConfigYaml="${SCENARIO_HOMEDIR}/${consumerSubFolder}/application.yml"
cp ${springConfigYamlTemplate} ${consumerConfigYaml}

replaceStringInFile "<TMPL_pulsar_broker_service_url>" "${brkrServiceUrl}" "${consumerConfigYaml}"
replaceStringInFile "<TMPL_pulsar_web_service_url>" "${webServiceUrl}" "${consumerConfigYaml}"
if [[ ${jwtTokenAuthEnabled} -eq 1 ]]; then
    replaceStringInFile \
        "<TMPL_auth_method>" \
        "org.apache.pulsar.client.impl.auth.AuthenticationToken" \
        "${consumerConfigYaml}"
    replaceStringInFile \
        "<TMPL_auth_parameters>" \
        "token: ${jwtTokenValue}" \
        "${consumerConfigYaml}"
else
    replaceStringInFile "<TMPL_auth_method>" "" "${consumerConfigYaml}"
    replaceStringInFile "<TMPL_auth_parameters>" "" "${consumerConfigYaml}"
fi

replaceStringInFile "<TMPL_num_message>" "${msgNum}" "${consumerConfigYaml}"
replaceStringInFile "<TMPL_full_topic_name>" "${tpName}" "${consumerConfigYaml}"

# generate a random alphanumeric string with length 20
randomStr=$(cat /dev/urandom | LC_ALL=C tr -dc 'a-zA-Z0-9' | fold -w 20 | head -n 1)
replaceStringInFile "<TMPL_subscription_name>" "myspbp-sub-${randomStr}" "${consumerConfigYaml}"


#####
## 2. Run the producer Spring Boot application
#####
javaCmd="java \
   -cp ${clientAppJar} \
   com.example.pulsarworkshop.IoTSensorSpbpConsumer \
   --spring.config.location=${SCENARIO_HOMEDIR}/${consumerSubFolder}/"
debugMsg="javaCmd=${javaCmd}"

eval ${javaCmd}
