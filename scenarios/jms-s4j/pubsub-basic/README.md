- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Program List](#11-program-list)
    - [1.1.1. Build the Program](#111-build-the-program)
  - [1.2. Software Requirement](#12-software-requirement)
  - [1.3. JMS Topic](#13-jms-topic)
    - [1.3.1. Customize the JMS Topic](#131-customize-the-jms-topic)
- [2. Connect to the Pulsar Cluster](#2-connect-to-the-pulsar-cluster)
- [3. IoT Sensor Reading Data Source](#3-iot-sensor-reading-data-source)
- [4. Deploy the Scenario](#4-deploy-the-scenario)
- [5. Run the Scenario](#5-run-the-scenario)
  - [5.1. Run the JMS Topic Subscriber Client App](#51-run-the-jms-topic-subscriber-client-app)
  - [5.2. Run the JMS Topic Publisher Client App](#52-run-the-jms-topic-publisher-client-app)


# 1. Scenario Overview

| | |
| - | - |
| **Name** | p2p-basic |
| **Description** | This scenario shows how to use the Starlight for JMS (S4J) API with Pulsar to do native message sending and receiving with a JMS topic. |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [JMS Topic Publisher] -> (JMS topic) -> [JMS Topic Subscriber] |

## 1.1. Program List

There are 2 programs used in this scenario to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Source Code | Description |
| ---- | ----------- | ----------- |
| IoTSensorTopicSubscriber | [IoTSensorTopicSubscriber.java](./src/main/java/com/example/pulsarworkshop/IoTSensorTopicSubscriber.java) | A JMS publisher client app that reads data from an IoT reading data source file (CSV format) and publishes the data to a JMS topic which is backed by a Pulsar topic behind the scene. |
| IoTSensorTopicPublisher | [IoTSensorTopicPublisher.java](./src/main/java/com/example/pulsarworkshop/IoTSensorTopicPublisher.java) | A JMS subscriber client app that subscribes messages from a JMS topic which is backed by a Pulsar topic behind the scene. |

### 1.1.1. Build the Program

The above programs need to be built in advance before running this scenario. Please refer to the document of [Building the Scenarios](../../../Build.Programs.md) for more details.

## 1.2. Software Requirement

Running the scenario require the following software to be installed on your local computer:

1. `JDK 11`
2. `curl` utility

## 1.3. JMS Topic

In Pulsar, a JMS topic is backed by a Pulsar topic. Therefore, running this scenario requires the following default Pulsar tenant, namespace, and topic. 

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/p2p_s4j`

Please **NOTE** that the creation of the above Pulsar "resources" can be **automated** by using the `deploy.sh` scrip. (see [Chapter 4](#4-deploy-the-scenario))

### 1.3.1. Customize the JMS Topic

If you want to run this scenario against a different set of Pulsar tenant, namespace, and topics, it can also be achieved by using a more advanced functionality of the `deploy.sh` script, via a `deployment properties` file. The document of [Deploying the Scenario](Deploy.Scenario.md) provides more details of how to do so.

# 2. Connect to the Pulsar Cluster

Both the Pulsar producer and consumer client apps get the connection info to the target Pulsar cluster from a `client.conf` file as described in this [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client).

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 3. IoT Sensor Reading Data Source

The CSV file that contains the raw IoT sensor reading data is available from [sensor_telemetry.csv](../../_raw_data_src/sensor_telemetry.csv). Each line of the CSV file represents a particular IoT device reading of the following types at a particular time.
* Carbon monoxide
* Humidity (%)
* Light detection
* Liquefied petroleum gas (LPG)
* Motion detection
* Smoke
* Temperature

For a more detailed description of this data source, please check from [here](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k).

# 4. Deploy the Scenario

The scenario deployment script, [`deploy.sh`](_bash/deploy.sh), is used to execute the following tasks which are required before running the scenario.
1. Create the required Pulsar tenant (only relevant for non-Astra Streaming based Pulsar cluster)
2. Create the required Pulsar namespace
3. Create the required Pulsar topic

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

## 5.1. Run the JMS Topic Subscriber Client App

The following script [`runConsumer.sh`](_bash/runConsumer.sh) is used to run the JMS receiver client app that receives the messages from the JMS topic, `msgenrich/testns/s4j_pubsub`.

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
runConsumer.sh -cc /tmp/client.conf -n 100 -t msgenrich/testns/s4j_pubsub
```

## 5.2. Run the JMS Topic Publisher Client App

The following script [`runProducer.sh`](_bash/runProducer.sh) is used to run the JMS sender client app that reads the IoT sensor data from a CSV source file and then sends them to the JMS topic, `msgenrich/testns/s4j_pubsub`.

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
runProducer.sh -cc /tmp/client.conf -n 100 -t msgenrich/testns/s4j_pubsub
```