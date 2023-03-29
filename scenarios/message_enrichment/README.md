- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Program List](#11-program-list)
  - [1.2. Prerequisite](#12-prerequisite)
  - [1.3. Build the Program](#13-build-the-program)
- [2. Run the Scenario](#2-run-the-scenario)
  - [2.1. IoT Reading Data Source](#21-iot-reading-data-source)
  - [2.2. Pulsar Cluster Connection](#22-pulsar-cluster-connection)
  - [2.3. Pulsar Tenants, Namespaces, and Topics](#23-pulsar-tenants-namespaces-and-topics)
    - [2.3.1. Bash Script](#231-bash-script)
  - [2.4. Deploy the Pulsar Function](#24-deploy-the-pulsar-function)
    - [2.4.1. Bash Script](#241-bash-script)
  - [2.5. Run Pulsar Consumer Client App](#25-run-pulsar-consumer-client-app)
    - [2.5.1. Bash Script](#251-bash-script)
  - [2.6. Run Pulsar Producer Client App](#26-run-pulsar-producer-client-app)
    - [2.6.1. Bash Script](#261-bash-script)


# 1. Scenario Overview

| | |
|-|-|
| **Name** | message-enrichment |
| **Description** | <ul><li>This scenario shows how to use a Pulsar function to enrich Pulsar messages that are published by a producer to a Pulsar topic.</li> <li>The enriched messages are sent by the function to another topic with a consumer subscribed to it for message consumption.</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer] -> (topic 1) -> [Pulsar Function] -> (topic 2) -> [Pulsar Consumer] |
|

## 1.1. Program List

There are 3 programs used in this scenario to demonstrate the end-to-end data flow pattern.

| Name | Type | Language | Source Code | Description |
| ---- | ---- | -------- | ----------- | ----------- |
| IoTSensorProducer | Pulsar client app | Java | [IotSensorProducer.java](./source_code/client_app/src/main/java/com/example/pulsarworkshop/IoTSensorProducer.java) | A Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic |
| AddMetadataFunc | Pulsar function | Java | [AddMetadataFunc.java](./source_code/function/src/main/java/com/example/pulsarworkshop/AddMetadataFunc.java) | A Pulsar function that adds a metadata property to each message of one topic and publishes a new message to another topic for further processing |
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

# 2. Run the Scenario

## 2.1. IoT Reading Data Source

The CSV file that contains the raw IoT sensor reading data is available from [sensor_telemetry.csv](../_raw_data_src/sensor_telemetry.csv). Each line of the CSV file represents a particular IoT device reading of the following types at a particular time.
* Carbon monoxide
* Humidity (%)
* Light detection
* Liquefied petroleum gas (LPG)
* Motion detection
* Smoke
* Temperature

For a more detailed description of this data source, please check from [here](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k).

## 2.2. Pulsar Cluster Connection 

Both the Pulsar producer and consumer client apps get the connection info to the target Pulsar cluster from a `client.conf` file as described in this [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client).

Once such a file is prepared, put it under the scenario home directory and it will be needed by the Pulsar client applications included in this scenario.

## 2.3. Pulsar Tenants, Namespaces, and Topics

According to the data flow pattern, this scenario requires 2 Pulsar topics. You can use any Pulsar topics for this scenario; but for demonstration purpose, let's assume the following 2 Pulsar topics are needed:
* `persistent://mytest/default/t1`: This corresponds to `topic 1` in the data flow pattern and is used by the Pulsar producer to publish the IoT sensor reading to. It is also the input topic of the Pulsar function to add message metadata.
* `persistent://mytest/default/t2`: This corresponds to `topic 2` in the data flow pattern, and it is used by the Pulsar consumer to consumer messages from. It is also the output topic of the Pulsar function that contains the enriched messages. 

**NOTE**: make sure the corresponding Pulsar tenant `mytest` and the Pulsar namespace `mytest/default` are in place before creating the topics.

### 2.3.1. Bash Script

The following script [`prepScenario.sh`](appexec/prepScenario.sh) is used to create the required Pulsar tenant(s), namespace(s), and topic(s) in this scenario.

## 2.4. Deploy the Pulsar Function

Before the Pulsar consumer can read the expected data, the Pulsar function needs to be deployed. We'll use the following Pulsar function configuration to deploy a Pulsar function named `add-metadata`

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

### 2.4.1. Bash Script

The following script [`deployFunc_add-metadata.sh`](appexec/deployFunc_add-metadata.sh) is used to deploy the above function in the target Pulsar cluster.

## 2.5. Run Pulsar Consumer Client App

The Pulsar consumer client app, `IoTSensorConsumer`, is CLI based Java application that takes the following parameters.

```
usage: IoTSensorConsumer [-a] [-c <arg>] [-h] [-n <arg>] [-sbn <arg>] [-sbt <arg>] [-t <arg>]
Command Line Options:
  -a,--astra           Whether to use Astra streaming.
  -c,--connFile <arg>  "client.conf" file path.
  -h,--help            Displays the usage method.
  -n,--numMsg <arg>    Number of messages to process.
  -sbn,--subName <arg> Pulsar subscription name.
  -sbt,--subType <arg> Pulsar subscription type.
  -t,--topic <arg>     Pulsar topic name.
```

### 2.5.1. Bash Script

The following script [`runApp_IoTSensorConsumer.sh`](appexec/runApp_IoTSensorConsumer.sh) is used to run the Pulsar consumer client app to consumer messages from `topic 2` that contains the enriched messages.

## 2.6. Run Pulsar Producer Client App

The Pulsar consumer client app, `IoTSensorProducer`, is CLI based Java application that takes the following parameters.

```
usage: IoTSensorProducer [-a] [-c <arg>] [-csv <arg>] [-h] [-n <arg>] [-t <arg>]
Command Line Options:
  -a,--astra           Whether to use Astra streaming.
  -c,--connFile <arg>  "client.conf" file path.
  -csv,--csvFile <arg> IoT sensor data CSV file.
  -h,--help            Displays the usage method.
  -n,--numMsg <arg>    Number of messages to process.
  -t,--topic <arg>     Pulsar topic name.
```

### 2.6.1. Bash Script

The following script [`runApp_IoTSensorProducer.sh`](appexec/runApp_IoTSensorProducer.sh) is used to run the Pulsar producer client app that reads the IoT sensor reading data and publishes to `topic 1` that contains the original messages before enrichment.