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
