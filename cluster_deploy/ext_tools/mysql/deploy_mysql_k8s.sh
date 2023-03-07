
#! /bin/bash

###
# Copyright DataStax, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###


CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PULSAR_WORKSHOP_HOMEDIR=$( cd -- "${CUR_SCRIPT_FOLDER}/.." &> /dev/null && pwd )

# TODO: Add usage stuff, parameter handling, etc.

setupMysqlDebeziumDemo() { 


# TODO: Make sure we have built project before we call this script since it requires Jars to be available, so make sure we're calling this function in the right place during deployment.

# Expecting first parameter to be the scenario name:
scnName=$1


source "${PULSAR_WORKSHOP_HOMEDIR}/_bash_utils_/utilities.sh"

scnHomeDir="${PULSAR_WORKSHOP_HOMEDIR}/scenarios/${scnName}"

MYSQL_PASSWORD=mysqlpw
MYSQL_ROOT_PASSWORD=debezium
MYSQL_IMAGE=debezium/example-mysql:2.1.2.Final
PULSAR_VERSION=2.11.0
REPLICATOR_JAR=debezium_replicator_producer_consumer-1.0.0.jar

kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secret
type: kubernetes.io/basic-auth
stringData:
  password: ${MYSQL_PASSWORD}
  rootPassword: ${MYSQL_ROOT_PASSWORD}
EOF

kubectl apply -f - <<EOF
apiVersion: v1
kind: PersistentVolume
metadata:
  name: mysql-pv-volume
  labels:
    type: local
spec:
  storageClassName: manual
  capacity:
    storage: 20Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/mnt/data"
EOF

kubectl apply -f - <<EOF
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pv-claim
spec:
  storageClassName: manual
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
EOF

kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-debezium-config
data:
  debezium-mysql-source-config.yaml: |
    tenant: "public"
    namespace: "default"
    name: "debezium-mysql-source"
    topicName: "mysql-connect-topic"
    parallelism: 1

    configs:
      ## sourceTask
      task.class: "io.debezium.connector.mysql.MySqlConnectorTask"

      ## config for mysql:
      database.hostname: "localhost"
      database.port: "3306"
      database.user: "debezium"
      database.password: "dbz"
      database.server.id: "184054"
      database.server.name: "dbserver1"
      database.whitelist: "inventory"

      database.history: "org.apache.pulsar.io.debezium.PulsarDatabaseHistory"
      database.history.pulsar.topic: "history-topic"
      database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
      ## KEY_CONVERTER_CLASS_CONFIG, VALUE_CONVERTER_CLASS_CONFIG
      key.converter: "org.apache.kafka.connect.json.JsonConverter"
      value.converter: "org.apache.kafka.connect.json.JsonConverter"
      ## PULSAR_SERVICE_URL_CONFIG
      pulsar.service.url: "pulsar://127.0.0.1:6650"
      ## OFFSET_STORAGE_TOPIC_CONFIG
      offset.storage.topic: "offset-topic"
EOF

# TODO: Next, we need to substitute in the Jar that we built for this scenario:
gsutil cp ${scnHomeDir}/appexec/package/${SCENARIO_NAME}.jar gs://temp-storage-for-pulsar-demos

# Need to create a GCP cloud bucket or find a way to upload it to the pod

kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
spec:
  selector:
    matchLabels:
      app: mysql
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - image: ${MYSQL_IMAGE}
        name: mysql
        env:
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: rootPassword
        ports:
        - containerPort: 3306
          name: mysql
        volumeMounts:
        - name: mysql-persistent-storage
          mountPath: /var/lib/mysql
        - name: mysql-debezium-config
          mountPath: /pulsar/conf/debezium/debezium-mysql-source-config.yaml
        lifecycle:
          postStart:
            exec:
              command: ["/bin/sh", "-c", "curl -O https://archive.apache.org/dist/pulsar/pulsar-${PULSAR_VERSION}/apache-pulsar-${PULSAR_VERSION}-bin.tar.gz; tar -zxvf ./apache-pulsar-${PULSAR_VERSION}-bin.tar.gz; cd ./apache-pulsar-${PULSAR_VERSION}/; mkdir connectors; cd connectors; curl -O https://archive.apache.org/dist/pulsar/pulsar-${PULSAR_VERSION}/connectors/pulsar-io-debezium-mysql-${PULSAR_VERSION}.nar; cd ..; bin/pulsar standalone &; bin/pulsar-admin source create  --sourceConfigFile /pulsar/conf/debezium/debezium-mysql-source-config.yaml --archive ./connectors/pulsar-io-debezium-mysql-${PULSAR_VERSION}.nar; gsutil cp gs://temp-strage-for-pulsar-demos/${REPLICATOR_JAR}.jar .; chmod u+x ${REPLICATOR_JAR}.jar; java -jar ${REPLICATOR_JAR}.jar"]
      volumes:
      - name: mysql-persistent-storage
        persistentVolumeClaim:
          claimName: mysql-pv-claim
      - name: mysql-debezium-config
        configMap:
          name: mysql-debezium-config
EOF

kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  ports:
  - port: 3306
  selector:
    app: mysql
EOF


kubectl exec pod/$(kg pods -o=jsonpath='{.items[?(@.metadata.labels.app=="mysql")].metadata.name}') -- bash -c "mysql -u mysqluser -p${MYSQL_PASSWORD} inventory -e \"UPDATE products SET name='111114511111' WHERE id=101;\""



}