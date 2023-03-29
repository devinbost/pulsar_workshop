#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

java \
  -cp ${SCENARIO_HOMEDIR}/source_code/client_app/target/msgenrich-clientapp-1.0.0.jar \
  com.example.pulsarworkshop.IoTSensorProducer \
  -a -n 10 -t ymtest/default/t1 -c ${SCENARIO_HOMEDIR}/client.conf -csv ${SCENARIO_HOMEDIR}/../_raw_data_src/sensor_telemetry.csv