#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

echo "SCENARIO_HOMEDIR=${SCENARIO_HOMEDIR}"

source "${SCENARIO_HOMEDIR}/../../_bash/utilities.sh"
# DEBUG=true

echo

##
# Show usage info 
#
usage() {   
   echo
   echo "Usage: deploy.sh [-h]" 
   echo "                 -cc <client_conf_file>" 
   echo "                 -dp <deploy_properties_file>"
   echo "                 [-na]"
   echo "       -h  : Show usage info"
   echo "       -cc : (Required) 'client.conf' file path."
   echo "       -dp : (Optional) 'deploy.properties' file path (default to '<SCENARIO_HOMEDIR>/deploy.properties')."
   echo "       -na : (Optional) Non-Astra Streaming (Astra streaming is the default)."
   echo
}

if [[ $# -eq 0 || $# -gt 6 ]]; then
   usage
   errExit 10 "Incorrect input parametere count!"
fi

astraStreaming=1
while [[ "$#" -gt 0 ]]; do
   case $1 in
      -h) usage; exit 0 ;;
      -cc) clntConfFile=$2; shift    ;;
      -na) astraStreaming=0;   ;;
      -dp) deployPropFile=$2; shift  ;;
      *)  errExit 20 "Unknown input parameter passed: $1" ;;
   esac
   shift
done
debugMsg "astraStreaming=${astraStreaming}"
debugMsg "clntConfFile=${clntConfFile}"
debugMsg "deployPropFile=${deployPropFile}"

if ! [[ -f "${clntConfFile}" ]]; then
   errExit 30 "The specified 'client.conf' file is invalid!"
fi
brkrServiceUrl=$(getPropVal ${clntConfFile} "brokerServiceUrl")
webServiceUrl=$(getPropVal ${clntConfFile} "webServiceUrl")
authPlugin=$(getPropVal ${clntConfFile} "authPlugin")
authParams=$(getPropVal ${clntConfFile} "authParams")
debugMsg "brkrServiceUrl=${brkrServiceUrl}"
debugMsg "webServiceUrl=${webServiceUrl}"
debugMsg "authPlugin=${authPlugin}"
debugMsg "authParams=${authParams}"

# Get JWT_TOKEN value if JWT token authentication is enabled
# TBD: this assumes the target cluster must use the JWT token authentication method
if ! [[ -z "${authPlugin}" ]]; then
    jwtTokenAuthEnabled=1
    IFS=':' read -r -a tokenStrArr <<< "${authParams}"
    jwtTokenValue="${tokenStrArr[1]}"
    debugMsg "jwtTokenValue=${jwtTokenValue}"
fi
if [[ ${jwtTokenAuthEnabled} -eq 1 && -z "${jwtTokenValue// }" ]]; then
    errExit 40 "Missing JWT token value in the specified 'client.conf' file when JWT token authentication is enabled!"
fi

dftDeployPropFile="${SCENARIO_HOMEDIR}/deploy.properties"
if ! [[ -f "${deployPropFile}" ]]; then
    deployPropFile=${dftDeployPropFile}
fi
if ! [[ -f "${deployPropFile}" ]]; then
    errExit 50 "The specified 'deploy.properties' file is invalid!"
fi
# in format <tenant>/<namespace>
tntNamespace=$(getPropVal ${deployPropFile} "tenantNamespace")
coreTopics=$(getPropVal ${deployPropFile} "coreTopics")
debugMsg "tntNamespace=${tntNamespace}"
debugMsg "coreTopics=${coreTopics}"

IFS='/' read -r -a tntNsArr <<< "${tntNamespace}"
tenant="${tntNsArr[0]}"
namespace="${tntNsArr[1]}"
if [[ -z "${tenant// }" || -z "${namespace// }" ]]; then
    errExit 60 "Must specify a valid value for 'tenantNamespace' config in the deploy properties file!"
fi

# array of full topic names (in format: <tenant>/<namespace>/<topic>)
pulsarTopics=()
if [[ -z "${coreTopics// }" ]]; then
    errExit 70 "Must specify at least one topic for 'coreTopics' config in the deploy properties file!"
else
    IFS=',' read -r -a coreTpArr <<< "${coreTopics}"
    for ctp in "${coreTpArr[@]}"; do
        pulsarTopics+=("${tenant}/${namespace}/${ctp}")
    done
fi

startDate=$(date +'%Y-%m-%d')
deployLogHomeDir="${SCENARIO_HOMEDIR}/logs/${startDate}"
deployMainLogFile="${deployLogHomeDir}/deployMain.log"
if ! [[ -d "${deployLogHomeDir}" ]]; then
   mkdir -p ${deployLogHomeDir}
fi


echo > "${deployMainLogFile}"


curlExistence=$(chkSysSvcExistence curl)
debugMsg "curlExistence=${curlExistence}"
if [[ ${curlExistence} -eq 0 ]]; then
    errExit 90 "[ERROR] 'curl' isn't installed on the local machine, which is required to create Pulsar topics and functions!"
fi


#####
## 1. Create the Pulsar tenant (only relevant with non-Astra streaming)
#####
if [[ ${astraStreaming} -eq 0 ]]; then
    clusterName=$(getPropVal ${deployPropFile} "nas.clusterName")
    if [[ -z "${clusterName}" ]]; then
        errExit 100 "[ERROR] Must specify a valid value for 'nas.clusterName' config in the deploy properties file!"
    fi

    outputMsg "" 0 ${deployMainLogFile} true
    outputMsg ">>> Creating the required Pulsar tenant (\"${tenant}\") ..." 0 ${deployMainLogFile} true
    httpResponseCode=$(createTenant \
        "${webServiceUrl}" \
        "${clusterName}" \
        "${tenant}" \
        "${jwtTokenAuthEnabled}" \
        "${jwtTokenValue}")
    processHttpResponse "${httpResponseCode}" 6 ${deployMainLogFile}
    sleep 1
fi


#####
## 2. Create the corresponding namespace
#####
outputMsg "" 0 ${deployMainLogFile} true
outputMsg ">>> Creating the required Pulsar namespace (\"${tenant}/${namespace}\") ..." 0 ${deployMainLogFile} true
httpResponseCode=$(createNameSpace \
    "${webServiceUrl}" \
    "${tenant}" \
    "${namespace}" \
    "${jwtTokenAuthEnabled}" \
    "${jwtTokenValue}")
processHttpResponse "${httpResponseCode}" 6 ${deployMainLogFile}
sleep 1


#####
## 3. Create the Pulsar topics
#####
if [[ ${#pulsarTopics[@]} -gt 0 ]]; then
    outputMsg "" 0 ${deployMainLogFile} true
    outputMsg ">>> Creating the required Pulsar topics ..." 0 ${deployMainLogFile} true
    topicIdx=0
    for topic in "${pulsarTopics[@]}"; do
        topicIdx=$((topicIdx+1))
        outputMsg "* Topic ${topicIdx} : ${topic}" 4 ${deployMainLogFile} true
        httpResponseCode=$(createTopic \
            "${webServiceUrl}" \
            "${topic}" \
            "${jwtTokenAuthEnabled}" \
            "${jwtTokenValue}")
        processHttpResponse "${httpResponseCode}" 6 ${deployMainLogFile}

        outputMsg "" 0 ${deployMainLogFile} true
    done
fi

echo