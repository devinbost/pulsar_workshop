#! /bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIO_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

if [[ -z "${JWT_TOKEN}" ]]; then
    echo "Please set the 'JWT_TOKEN' value to connect to the target Pulsar cluster!"
    exit
fi

# create a topic with 5 partitions
curl -v -k -X POST \
    --url 'https://pulsar-gcp-uscentral1.api.streaming.datastax.com/admin/v2/persistent/ymtest/default/t1/partitions' \
    --header 'Authorization: Bearer ${JWT_TOKEN}' \
    --header 'Content-Type: text/plain' \
    --data '5'

curl -v -k -X POST \
    --url 'https://pulsar-gcp-uscentral1.api.streaming.datastax.com/admin/v2/persistent/ymtest/default/t2/partitions' \
    --header 'Authorization: Bearer ${JWT_TOKEN}' \
    --header 'Content-Type: text/plain' \
    --data '5'