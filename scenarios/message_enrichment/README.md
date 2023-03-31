- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Program List](#11-program-list)
  - [1.2. Prerequisite](#12-prerequisite)
  - [1.3. Build the Program](#13-build-the-program)
- [2. IoT Sensor Reading Data Source](#2-iot-sensor-reading-data-source)
- [3. Connect to the Pulsar Cluster](#3-connect-to-the-pulsar-cluster)
- [4. Deploy the Scenario](#4-deploy-the-scenario)
  - [4.1. Deployment Properties File](#41-deployment-properties-file)
  - [4.2. Pulsar Rest API](#42-pulsar-rest-api)
  - [4.3. Create Tenant](#43-create-tenant)
  - [4.4. Create Namespace](#44-create-namespace)
  - [4.5. Create Topic](#45-create-topic)
  - [4.6. Deploy Function](#46-deploy-function)
- [5. Run the Scenario](#5-run-the-scenario)
  - [5.1. Run Pulsar Consumer Client App](#51-run-pulsar-consumer-client-app)
  - [5.2. Run Pulsar Producer Client App](#52-run-pulsar-producer-client-app)
- [6. Example: Step-by-Step Procedure of Deploying and Running this Scenario](#6-example-step-by-step-procedure-of-deploying-and-running-this-scenario)
  - [6.1. Astra Streaming Cluster](#61-astra-streaming-cluster)
  - [6.2. Non-Astra Streaming Cluster](#62-non-astra-streaming-cluster)


# 1. Scenario Overview

| | |
| - | - |
| **Name** |message-enrichment |
| **Description** | <ul><li>This scenario shows how to use a Pulsar function to enrich Pulsar messages that are published by a producer to a Pulsar topic.</li> <li>The enriched messages are sent by the function to another topic with a consumer subscribed to it for message consumption.</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer] -> (topic 1) -> [Pulsar Function] -> (topic 2) -> [Pulsar Consumer] |

## 1.1. Program List

There are 3 programs used in this scenario to demonstrate the end-to-end data flow pattern.

| Name | Type | Language | Source Code | Description |
| ---- | ---- | -------- | ----------- | ----------- |
| IoTSensorProducer | Pulsar client app | Java | [IotSensorProducer.java](./source_code/client_app/src/main/java/com/example/pulsarworkshop/IoTSensorProducer.java) | A Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic. |
| AddMetadataFunc | Pulsar function | Java | [AddMetadataFunc.java](./source_code/function/src/main/java/com/example/pulsarworkshop/AddMetadataFunc.java)       | A Pulsar function that adds a metadata property to each message of one topic and publishes a new message to another topic for further processing. |
| IoTSensorConsumer | Pulsar client app | Java | [IotSensorConsumer.java](./source_code/client_app/src/main/java/com/example/pulsarworkshop/IoTSensorConsumer.java) | A standard Pulsar consumer client app that consumes from a topic that contains the processed messages with the new metadata property information. |

## 1.2. Prerequisite

Building and running the scenario require the following software to be installed on your local computer:

1. `JDK 11`
2. `curl` utility
3. [`Apache Maven`](https://maven.apache.org/)


## 1.3. Build the Program

Run the following command in the `source_code` folder, and it will build all three programs (2 Pulsar client applications and 1 Pulsar function)
```
mvn clean verify
```

This step can also be done by running the following deployment script, `deploy.sh` (more on this later) with `-b` option.


# 2. IoT Sensor Reading Data Source

The CSV file that contains the raw IoT sensor reading data is available from [sensor_telemetry.csv](../_raw_data_src/sensor_telemetry.csv). Each line of the CSV file represents a particular IoT device reading of the following types at a particular time.
* Carbon monoxide
* Humidity (%)
* Light detection
* Liquefied petroleum gas (LPG)
* Motion detection
* Smoke
* Temperature

For a more detailed description of this data source, please check from [here](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k).

# 3. Connect to the Pulsar Cluster

Both the Pulsar producer and consumer client apps get the connection info to the target Pulsar cluster from a `client.conf` file as described in this [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client).

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 4. Deploy the Scenario

In order to run the demo scenario, there are certain tasks that need to be executed in advance which include
* For non-Astra Streaming based Pulsar cluster, create a tenant
* Create a namespace 
* Create the required topics
* Deploy the required functions

The script, [deploy.sh](bash/deploy.sh), is used to simplify and automate the deployment procedure.
```
Usage: deploy.sh [-h]
                 [-b]
                 [-na]
                 -cc <client_conf_file>
                 -dp <deploy_properties_file>
       -h  : Show usage info
       -b  : (Optional) Build demo applications when specified.
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -cc : (Required) 'client.conf' file path.
       -dp : (Optional) 'deploy.properties' file path (default to '<SCENARIO_HOMEDIR>/deploy.properties').
```

The input parameters of this script are quite straightforward and self-explanatory. The only one that needs a bit of explanation is the `-dp` option as below.

## 4.1. Deployment Properties File

In order to make the deployment script generic enough to support arbitrary Pulsar tenant, namespace, topics, or functions, we use a properties file to define these "changing" parts.
```
##
# (Mandatory) Must in format <tenant>/<namespace>
tenantNamespace=ymtest/default
# (Mandatory) Comma separated core topic names without space
coreTopics=t1,t2

## 
# (Optional) Comma separated core function names without space
coreFunctions=add-metadata
# (Optional) Function package file name
funcPkgName=msgenrich-function-1.0.0.jar

##
# Cluster name (ONLY relevant for non-Astra Streaming Pulsar cluster)
nas.clusterName=mypulsar
```

Based on the above information, the `deploy.sh` will create and deploy all the required Pulsar resources for the demo scenario execution.

## 4.2. Pulsar Rest API 

The `deploy.sh` script creates all Pulsar resources via the Pulsar rest API through the `curl` command. The benefit of doing so is you don't need to download or install any Pulsar admin client tools like *pulsar-admin* or *pulsar-shell*. Using these tools to create the corresponding Pulsar resources is easy and straightforward. Please refer to the [Pulsar Admin CLI doc](https://pulsar.apache.org/docs/2.11.x/reference-pulsar-admin/)

## 4.3. Create Tenant

**NOTE**: The `deploy.sh` script will ONLY execute this step for non-AS based Pulsar deployment and this requires explicitly setting `deploy.sh -na` option because by default `deploy.sh` assumes dealing with an AS Pulsar cluster.

The rest API to create a Pulsar tenant is as below:
```
curl -sS -k -X PUT \
  --url 'https://<pulsar_websvc_url>/admin/v2/tenants/<tenant_name> \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <jwt_token>' \
  --data '{ \"allowedClusters\": [\"<cluster_name>\"] }'
```

## 4.4. Create Namespace

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/namespaces/<tenant_name>/<namespace_name>' \
    --header 'Authorization: Bearer <jwt_token>'
```

## 4.5. Create Topic

**NOTE** The `deploy.sh` script will always create a partitioned topic with 5 partitions, which should be good enough for the common demo scenarios.

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/persistent/${topicName}/partitions' \
    --header 'Authorization: Bearer <jwt_token>' \
    --header 'Content-Type: text/plain' \
    --data 5
```

## 4.6. Deploy Function

The rest API to deploy a Pulsar function is as below:
```
curl -sS -k -X POST \
    --url 'https://<pulsar_websvc_url>/admin/v3/functions/ymtest/default/<function_name>' \
    --header 'Authorization: Bearer <jwt_token> \
    --form 'data=@</path/to/function/jar/file>;type=application/octet-stream' \
    --form 'functionConfig=@</path/to/to/function/config/json/file>;type=application/json' \
    --write-out '%{http_code}'
```

In order to deploy a Pulsar function this way, a function configuration JSON file is needed besides the Pulsar function package file (e.g. a Java jar file). An example of this configuration file is as below:
```
{
  "tenant": "ymtest",
  "namespace": "default",
  "name": "add-metadata",
  "runtime": "JAVA",
  "inputs": [ "ymtest/default/t1" ],
  "output": "ymtest/default/t2",
  "autoAck": true,
  "className": "com.example.pulsarworkshop.AddMetadataFunc",
  "jar": ""
}
```

Please **NOTE** that the `deploy.sh` file will NOT create this file for you. You need to create it in advance before running the script. The requirements for this file are:
1. The JSON file must use the function name as the file.
2. The JSON file must be under `config` sub-folder of the scenario home directory
```
config
└── add-metadata.json
```

# 5. Run the Scenario

After all Pulsar resources are deployed, we can run the Pulsar client applications included in this scenario.

## 5.1. Run Pulsar Consumer Client App

The following script [`runConsumer.sh`](bash/runConsumer.sh) is used to run the Pulsar consumer client app that consumes the enriched messages from `topic 2`.

```
Usage: runConsumer.sh [-h]
                      [-na]
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -n  : (Required) The number of messages to consume.
       -cc : (Required) 'client.conf' file path.
```

## 5.2. Run Pulsar Producer Client App

The following script [`runProducer.sh`](bash//runProducer.sh) is used to run the Pulsar producer client app that reads the IoT sensor data from a CSV source file and then publishes to `topic 1`.

```
Usage: runProducer.sh [-h]
                      [-na]
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -n  : (Required) The number of messages to produce.
       -cc : (Required) 'client.conf' file path.
```

# 6. Example: Step-by-Step Procedure of Deploying and Running this Scenario

In this example, we assume that we want to run the scenario against the following Pulsar tenant, namespace, and topics
* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/raw`
   * `msgenrich/testns/processed`

## 6.1. Astra Streaming Cluster

The procedure of deploying and running this scenario in an AS cluster is as below:

1. From the AS UI, create the tenant `msgenrich`
2. Download/copy the `client.conf` for that tenant to a local folder, say `/tmp`
3. Update the Pulsar tenant, namespace, and topic information in the scenario deployment properties file, `deploy.properties`. Other information stays the same.
```
tenantNamespace=msgenrich/testns
coreTopics=raw,processed
... ...
```
4. Update the Pulsar tenant, namespace, and topic information in the function configuration JSON file, `config/add-metadata.json`. Other information stays the same.
```
{
  "tenant": "msgenrich",
  "namespace": "testns",
  ... ...
  "inputs": [ "msgenrich/testns/raw" ],
  "output": "msgenrich/testns/processed",
  ... ...
}
```
5. Run the deployment script file. Use `-b` option if you want to rebuild the programs.
```
deploy.sh -cc /tmp/client.conf
```
6. Run the consumer client application, assuming to receiving 100 messages from topic `msgenrich/testns/processed`
```
runConsumer.sh -cc /tmp/client.conf -n 100 
```
1. Run the producer client application, assuming to read 100 IoT source data and publish them to topic `msgenrich/testns/processed`
```
runProducer.sh -cc /tmp/client.conf -n 100 
```
 
## 6.2. Non-Astra Streaming Cluster

The steps in a non-Astra Streaming cluster is almost the same with the following differences

1. step **1** is not needed
2. In step **3**, please make sure Pulsar cluster name (e.g. *mypulsar*) is included in the deployment properties file, `deploy.properties`
```
... ...
nas.clusterName=mypulsar
```
3. In steps **5**, **6**, **7**, we need to provide `-na` option, as below
   * `deploy.sh -na -cc /tmp/client.conf`
   * `runConsumer.sh -na -cc /tmp/client.conf -n 100`
   * `runProducer.sh -na -cc /tmp/client.conf -n 100`