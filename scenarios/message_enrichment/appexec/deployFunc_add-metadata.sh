#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

# Please set this value
JWT_TOKEN=

if [[ -z "${JWT_TOKEN}" ]]; then
    echo "Please set the 'JWT_TOKEN' value to connect to the target Pulsar cluster!"
    exit
fi

curl -v -k -X POST \
    --url 'https://pulsar-gcp-uscentral1.api.streaming.datastax.com/admin/v3/functions/ymtest/default/add-metadata' \
    --header 'Authorization: Bearer ${JWT_TOKEN}' \
    --form "functionConfig=@${SCENARIO_HOMEDIR}/appexec/package/add-metadata.config.json;type=application/json" \
    --form "data=@${SCENARIO_HOMEDIR}/source_code/function/target/msgenrich-func-addmetadata-1.0.0.jar;type=application/octet-stream"
