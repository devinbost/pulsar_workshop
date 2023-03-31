#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

source "${SCENARIO_HOMEDIR}/../bash/utilities.sh"

echo

clientAppJar="${SCENARIO_HOMEDIR}/source_code/client_app/target/msgenrich-clientapp-1.0.0.jar"
if [[ -z "${clientAppJar}" ]]; then
  errExit 10 "Can't find the client app jar file. Please run 'deploy.sh -buildApp' to build it!"
fi

clientConfFile="${SCENARIO_HOMEDIR}/client.conf"
if ! [[ -f "${clientConfFile}" ]]; then
  errExit 20 "The specified 'client.conf' file is invalid!"
fi

java \
  -cp ${clientAppJar}  \
  com.example.pulsarworkshop.IoTSensorConsumer \
  -a -n 10 -t ymtest/default/t2 -c ${clientConfFile}  -sbn mysub
