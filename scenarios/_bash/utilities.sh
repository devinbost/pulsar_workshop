#! /bin/bash

DEBUG=false

##
# Show debug message 
# - $1 : the message to show
debugMsg() {
    if [[ "${DEBUG}" == "true" ]]; then
        if [[ $# -eq 0 ]]; then
            echo
        else
            echo "[Debug] $1"
        fi
    fi
}

##
# Exit bash execution with the specified return value
#
errExit() {
    echo "[ERROR] $2"
    echo
    exit $1
}
errExitNoMsg() {
    echo
    exit $1
}

##
# Change the working directory
# - $1: the directory to change to
chgWorkingDir() {
    cd $1
}

##
# Check if the required executeable (e.g. docker, kind) has been installed locally
#
chkSysSvcExistence() {
    local whichCmdOutput=$(which ${1})
    if [[ -z "${whichCmdOutput// }" ]]; then
        echo 0
    else
        echo 1
    fi   
}

##
# Read the properties file and returns the value based on the key
# 2 input prarameters:
# - 1st parameter: the property file to scan
# - 2nd parameter: the key to search for
getPropVal() {
    local propFile=$1
    local searchKey=$2
    local value=$(grep "${searchKey}" ${propFile} | grep -Ev "^#|^$" | cut -d'=' -f2)
    echo $value
}

repeatSpace() {
    head -c $1 < /dev/zero | tr '\0' ' '
}


##
# Output a message to a file with specified format
# 3 parameter: 
# - 1st parameter: the message to print for execution status purpose
# - (Optional) 2nd parameter: the number of the leading spaces
#              if not specified, there is no leading space
# - (Optional) 3rd parameter: the log file to append the message to
#              if not specified or not a valid file, print the message to stdout
# - (Optional) 4th parameter: indicates whether to write output to stdout as well
#              when appending to the file at the same time
#              * true: output to stdout while appending to the log file
#              * false: only append to the log file; do not write output to stdout
outputMsg() {
    if [[ $# -eq 0 || $# -gt 4 ]]; then
        echo "[Error] Incorrect usage of outputMsg()."
    else
        leadingSpaceStr=""
        if [[ -n $2 && $2 -gt 0 ]]; then
            leadingSpaceStr=$(repeatSpace $2)            
        fi

        if [[ -n "$3" && -f "$3" ]]; then
            echo "$leadingSpaceStr$1" >> $3
            if [[ "$4" == "true" ]]; then
                echo "$leadingSpaceStr$1"
            fi
        else
            echo "$leadingSpaceStr$1"
        fi
    fi
}


##
# NOTE: only applicable to LS, not AS
# ------------------------------------
# Check if a specified tenant already exists in the target Pulsar cluster
# 4 input parameters
#   - 1st parameter: Pulsar web service URL
#   - 2nd parameter: Pulsar tenant name
#   - 3rd parameter: Whether JWT token authentication is enabled
#   - 4th parameter: JWT token value
checkTntExistence () {
    local webSvcUrl=${1}
    local tenantName=${2}
    local jwtAuthEnalbed=${3}
    local tokenValue=${4}

    local curlCmd="curl -sS -k -X GET
    --url '${webSvcUrl}/admin/v2/tenants'"
                        
    if [[ ${jwtAuthEnalbed} -eq 1 ]]; then
        curlCmd="${curlCmd}
    --header 'Authorization: Bearer ${tokenValue}' "
    fi

    curlCmd="${curlCmd}
    --header 'Content-Type: text/plain'
    --data 5 
    --output '/tmp/curlCmdGetTntList.txt'"

    # stderr is needed here because stdout is capatured as the function output
    debugMsg "curlCmd=${curlCmd}" >&2
    local responseStr=$(eval ${curlCmd})

    local tntListStr=$(cat /tmp/curlCmdGetTntList.txt)
    debugMsg "resourceListStr=${tntListStr}"

    if [[ "${tntListStr}" =~ "${tenantName}" ]]; then
        return 1
    else
        return 0
    fi
}


##
# NOTE: only applicable to non-Astra streaming Pulsar clusters
# ------------------------------------
# Create a tenant using 'curl' command
# 5 input parameters
#   - 1st parameter: Pulsar web service URL
#   - 2nd parameter: Pulsar cluster name
#   - 3rd parameter: Pulsar tenant name 
#   - 4th parameter: Whether JWT token authentication is enabled
#   - 5th parameter: JWT token value
createTenant() {
    local webSvcUrl=${1}
    local clusterName=${2}
    local tenantName=${3}
    local jwtAuthEnalbed=${4}
    local tokenValue=${5}

    local curlCmd="curl -sS -k -X PUT
    --url '${webSvcUrl}/admin/v2/tenants/${tenantName}'
    --header 'Content-Type: application/json'
    --data '{ \"allowedClusters\": [\"${clusterName}\"] }'"
                        
    if [[ ${jwtAuthEnalbed} -eq 1 ]]; then
        curlCmd="${curlCmd}
    --header 'Authorization: Bearer ${tokenValue}'"
    fi

    # stderr is needed here because stdout is capatured as the function output
    debugMsg "curlCmd=${curlCmd}" >&2
    local responseStr=$(eval ${curlCmd})

    echo ${responseStr}
}

##
# Create a namespace using 'curl' command
# 5 input parameters
#   - 1st parameter: Pulsar web service URL
#   - 2nd parameter: Pulsar tenant name 
#   - 3rd parameter: Pulsar namespace name 
#   - 44h parameter: Whether JWT token authentication is enabled
#   - 5th parameter: JWT token value
createNameSpace() {
    local webSvcUrl=${1}
    local tenantName=${2}
    local namespaceName=${3}
    local jwtAuthEnalbed=${4}
    local tokenValue=${5}

    local curlCmd="curl -sS -k -X PUT
    --url '${webSvcUrl}/admin/v2/namespaces/${tenantName}/${namespaceName}'"
                        
    if [[ ${jwtAuthEnalbed} -eq 1 ]]; then
        curlCmd="${curlCmd}
    --header 'Authorization: Bearer ${tokenValue}'"
    fi

    # stderr is needed here because stdout is capatured as the function output
    debugMsg "curlCmd=${curlCmd}" >&2
    local responseStr=$(eval ${curlCmd})

    echo ${responseStr}
}


##
# Create a partitioned topic (with 5 partitions) using 'curl' command
# 4 input parameters
#   - 1st parameter: Pulsar web service URL
#   - 2nd parameter: Pulsar topic name (without the leading 'persistent://' or 'non-persistent://')
#   - 3rd parameter: Whether JWT token authentication is enabled
#   - 4th parameter: JWT token value
createTopic() {
    local webSvcUrl=${1}
    local topicName=${2}
    local jwtAuthEnalbed=${3}
    local tokenValue=${4}

    local curlCmd="curl -sS -k -X PUT
    --url '${webSvcUrl}/admin/v2/persistent/${topicName}/partitions'"
                        
    if [[ ${jwtAuthEnalbed} -eq 1 ]]; then
        curlCmd="${curlCmd}
    --header 'Authorization: Bearer ${tokenValue}' "
    fi

    curlCmd="${curlCmd}
    --header 'Content-Type: text/plain'
    --data 5"

    # stderr is needed here because stdout is capatured as the function output
    debugMsg "curlCmd=${curlCmd}" >&2
    local responseStr=$(eval ${curlCmd})

    echo ${responseStr}
}

##
# Create a partitioned topic (with 5 partitions) using 'curl' command
# 6 input parameters
#   - 1st parameter: Pulsar web service URL
#   - 2nd parameter: Pulsar topic name (without the leading 'persistent://' or 'non-persistent://')
#   - 3rd parameter: Whether JWT token authentication is enabled
#   - 4th parameter: JWT token value
#   - 5th parameter: Pulsar function jar file
#   - 6th parameter: Pulsar function config json file
createFunction() {
    local webSvcUrl=${1}
    local funcFullName=${2}     # <tenant>/<namespace>/<function>
    local jwtAuthEnalbed=${3}
    local tokenValue=${4}
    local funcPkgFile=${5}
    local funcCfgJsonFile=${6}

    local curlCmd="curl -sS -k -X POST
    --url '${webSvcUrl}/admin/v3/functions/${funcFullName}'"
                        
    if [[ ${jwtAuthEnalbed} -eq 1 ]]; then
        curlCmd="${curlCmd}
    --header 'Authorization: Bearer ${tokenValue}'"
    fi

    curlCmd="${curlCmd}
    --form 'data=@${funcPkgFile};type=application/octet-stream'
    --form 'functionConfig=@${funcCfgJsonFile};type=application/json'"

    # stderr is needed here because stdout is capatured as the function output
    debugMsg "curlCmd=${curlCmd}" >&2
    local responseStr=$(eval ${curlCmd})

    echo ${responseStr}
}

##
# Proces HTTP response string from the curl command
# 3 input parameters
#   - 1st parameter: The HTTP response string
#   - 2nd parameter: The number of the leading space number before each output message
#   - 3rd parameter: The log file to append the output messages to
processHttpResponse() {
    local responseStr=${1}
    local leadingSpaceNum=${2}
    local logFile=${3}

    responseStr=$(sed -e 's/^"//' -e 's/"$//' <<< ${responseStr})

    if [[ -z "${responseStr// }" ]]; then
        outputMsg "[Success]" ${leadingSpaceNum} ${logFile} true
    else
        outputMsg "[Failed] - ${responseStr}]" ${leadingSpaceNum} ${logFile} true
    fi
}

