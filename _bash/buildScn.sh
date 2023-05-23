#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/_bash/utilities.sh"
# DEBUG=true

echo

mvnExistence=$(chkSysSvcExistence mvn)
debugMsg "mvnExistence=${mvnExistence}"
if [[ ${mvnExistence} -eq 0 ]]; then
    errExit 10 "[ERROR] 'mvn' isn't installed on the local machine, which is required to build scenario programs!"
fi

jenvExistence=$(chkSysSvcExistence jenv)
debugMsg "jenvExistence=${jenvExistence}"
if [[ ${jenvExistence} -eq 0 ]]; then
    errExit 20 "[ERROR] 'jenv' isn't installed on the local machine, which is required to build scenario programs!"
fi


#####
## Build maven projects
#####
curFolder=$(pwd)

# Requires Java 11
mvnPrjFolder="scenarios"
if [[ -d "${SCENARIO_HOMEDIR}/${mvnPrjFolder}" && 
        -f "${SCENARIO_HOMEDIR}/${mvnPrjFolder}/pom.xml" ]]; then
    echo "Building maven project: ${mvnPrjFolder} ..."
    cd ${SCENARIO_HOMEDIR}/${mvnPrjFolder}
    # export JAVA_HOME=$(/usr/libexec/java_home -v 11)
    jenv local 11
    JAVA_HOME=${HOME}/.jenv/versions/$(cat .java-version)
    mvn clean install
else
    echo "Can not find maven project ${mvnPrjFolder}; skip building it!"
fi

# Requires Java 17
mvnPrjFolder="spring-scenarios"
if [[ -d "${SCENARIO_HOMEDIR}/${mvnPrjFolder}" && 
      -f "${SCENARIO_HOMEDIR}/${mvnPrjFolder}/pom.xml" ]]; then
    echo "Building maven project: ${mvnPrjFolder} ..."
    cd ${SCENARIO_HOMEDIR}/${mvnPrjFolder}
    # export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    jenv local 17
    JAVA_HOME=${HOME}/.jenv/versions/$(cat .java-version)
    mvn clean install
else
    echo "Can not find maven project ${mvnPrjFolder}; skip building it!"
fi


cd ${curFolder}


echo
