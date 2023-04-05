- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Program List](#11-program-list)
    - [1.1.1. Build the Program](#111-build-the-program)
  - [1.2. Software Requirement](#12-software-requirement)
  - [1.3. Pulsar Tenant, Namespace, and Topics](#13-pulsar-tenant-namespace-and-topics)
    - [1.3.1. Customize Pulsar Tenant, Namespace, and Topics](#131-customize-pulsar-tenant-namespace-and-topics)
- [2. Connect to the Pulsar Cluster](#2-connect-to-the-pulsar-cluster)
- [3. IoT Sensor Reading Data Source](#3-iot-sensor-reading-data-source)
- [4. Deploy the Scenario](#4-deploy-the-scenario)
- [5. Run the Scenario](#5-run-the-scenario)
  - [5.1. Run Pulsar Consumer Client App](#51-run-pulsar-consumer-client-app)
  - [5.2. Run Pulsar Producer Client App](#52-run-pulsar-producer-client-app)


# 1. Scenario Overview

| | |
| - | - |
| **Name** |message-enrichment |
| **Description** | <ul><li>This scenario shows how to use a Pulsar function to enrich Pulsar messages that are published by a producer to a Pulsar topic.</li> <li>The enriched messages are sent by the function to another topic with a consumer subscribed to it for message consumption.</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer] -> (topic 1) -> [Pulsar Function] -> (topic 2) -> [Pulsar Consumer] |

## 1.1. Program List

There are 3 programs used in this scenario to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Type | Source Code | Description |
| ---- | ---- | ----------- | ----------- |
| IoTSensorProducer | Pulsar client app | [IotSensorProducer.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorProducer.java) | A Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic. |
| AddMetadataFunc | Pulsar function | [AddMetadataFunc.java](./function/src/main/java/com/example/pulsarworkshop/AddMetadataFunc.java) | A Pulsar function that adds a metadata property to each message of one topic and publishes a new message to another topic for further processing. |
| IoTSensorConsumer | Pulsar client app | [IotSensorConsumer.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorConsumer.java) | A standard Pulsar consumer client app that consumes from a topic that contains the processed messages with the new metadata property information. |

### 1.1.1. Build the Program

The above programs need to be built in advance before running this scenario. Please refer to the document of [Building the Scenarios](../Build.Scenarios.md) for more details.

## 1.2. Software Requirement

Running the scenario require the following software to be installed on your local computer:

1. `JDK 11`
2. `curl` utility

## 1.3. Pulsar Tenant, Namespace, and Topics

By default, running this scenario requires the following Pulsar tenant, namespace, and topics. 

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/raw`
   * `msgenrich/testns/processed`

Please **NOTE** that the creation of the above Pulsar "resources" can be **automated** by using the `deploy.sh` scrip. (see [Chapter 4](#4-deploy-the-scenario))

### 1.3.1. Customize Pulsar Tenant, Namespace, and Topics

If you want to run this scenario against a different set of Pulsar tenant, namespace, and topics, it can also be achieved by using a more advanced functionality of the `deploy.sh` script, via a `deployment properties` file. The document of [Deploying the Scenario](Deploy.Scenario.md) provides more details of how to do so.

# 2. Connect to the Pulsar Cluster

Both the Pulsar producer and consumer client apps get the connection info to the target Pulsar cluster from a `client.conf` file as described in this [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client).

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 3. IoT Sensor Reading Data Source

The CSV file that contains the raw IoT sensor reading data is available from [sensor_telemetry.csv](../_raw_data_src/sensor_telemetry.csv). Each line of the CSV file represents a particular IoT device reading of the following types at a particular time.
* Carbon monoxide
* Humidity (%)
* Light detection
* Liquefied petroleum gas (LPG)
* Motion detection
* Smoke
* Temperature

For a more detailed description of this data source, please check from [here](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k).

# 4. Deploy the Scenario

The scenario deployment script, [`deploy.sh`](bash/deploy.sh), is used to execute the following tasks which are required before running the scenario.
1. Create the required Pulsar tenant (only relevant for non-Astra Streaming based Pulsar cluster)
2. Create the required Pulsar namespace
3. Create the required Pulsar topics
4. Deploy the required Pulsar function(s)

This script has the following usage format. The only mandatory parameter is `-cc` which is used to specify the required Pulsar cluster client connection file. The `-dp` parameter is related with the scenario deployment customization (see [`Deploying the Scenario`](Deploy.Scenario.md) doc for more details.)

```
Usage: deploy.sh [-h]
                 -cc <client_conf_file>
                 [-na]
                 [-dp <deploy_properties_file>]
       -h  : Show usage info
       -cc : (Required) 'client.conf' file path.
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -dp : (Optional) 'deploy.properties' file path (default to '<SCENARIO_HOMEDIR>/deploy.properties').
```

An example of using this script to deploy the scenario is as below:

```
deploy.sh -cc /tmp/client.conf
```

# 5. Run the Scenario

After all Pulsar resources are deployed, we can run the Pulsar client applications included in this scenario.

## 5.1. Run Pulsar Consumer Client App

The following script [`runConsumer.sh`](bash/runConsumer.sh) is used to run the Pulsar consumer client app that consumes the enriched messages from `topic 2`.

```
Usage: runConsumer.sh [-h]
                      [-na]
                      -t <topic_name>
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -t  : (Required) The topic name to publish messages to.
       -n  : (Required) The number of messages to consume.
       -cc : (Required) 'client.conf' file path.
```

An example of using this script to consuming 100 messages is as below:

```
runConsumer.sh -cc /tmp/client.conf -n 100 -t msgenrich/testns/processed
```

## 5.2. Run Pulsar Producer Client App

The following script [`runProducer.sh`](bash//runProducer.sh) is used to run the Pulsar producer client app that reads the IoT sensor data from a CSV source file and then publishes to `topic 1`.

```
Usage: runProducer.sh [-h]
                      [-na]
                      -t <topic_name>
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -t  : (Required) The topic name to publish messages to.
       -n  : (Required) The number of messages to produce.
       -cc : (Required) 'client.conf' file path.
```

An example of using this script to publish 100 messages is as below:

```
runProducer.sh -cc /tmp/client.conf -n 100 -t msgenrich/testns/raw
```