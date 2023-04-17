- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Program List](#11-program-list)
    - [1.1.1. Build the Program](#111-build-the-program)
  - [1.2. Software Requirement](#12-software-requirement)
  - [1.3. RabbitMQ on Pulsar](#13-rabbitmq-on-pulsar)
    - [1.3.1. Customize the RabbitMQ Queue](#131-customize-the-rabbitmq-queue)
- [2. Connect to the Pulsar Cluster](#2-connect-to-the-pulsar-cluster)
- [3. Deploy the Scenario](#3-deploy-the-scenario)
- [4. Run the Scenario](#4-run-the-scenario)
  - [4.1. Run the RabbitMQ S4R Queue Consumer Client App](#41-run-the-rabbitmq-s4r-queue-consumer-client-app)
  - [4.2. Run the RabbitMQ S4R Queue Producer Client App](#42-run-the-rabbitmq-s4r-queue-producer-client-app)


# 1. Scenario Overview

| | |
| - | - |
| **Name** | pubsub-queue |
| **Description** | This scenario shows how to use the Starlight for RabbitMQ (S4R) API with Pulsar to do native message sending and receiving with a RabbitMQ queue hosted on Pulsar. |
| **Data Flow Pattern** |  [S4R Queue Producer] -> (Pulsar RabbitMQ queue) -> [S4R Queue Consumer] |

## 1.1. Program List

There are 2 programs used in this scenario to demonstrate the end-to-end data flow pattern. These programs are written in **Java**. 

| Name | Source Code | Description |
| ---- | ----------- | ----------- |
| S4RQueueProducer | [S4RQueueProducer.java](./src/main/java/com/example/pulsarworkshop/S4RProducer.java) | A RabbitMQ producer client app that sends messages to a RabbitMQ queue which is backed by a Pulsar topic behind the scene. |
| S4RQueueConsumer | [S4RQueueConsumer.java](./src/main/java/com/example/pulsarworkshop/S4RQueueConsumer.java) | A RabbitMQ consumer client app that receives messages from a RabbitMQ queue which is backed by a Pulsar topic and subscription behind the scene. |

### 1.1.1. Build the Program

The above programs need to be built in advance before running this scenario. Please refer to the document of [Building the Scenarios](../../../Build.Programs.md) for more details.

## 1.2. Software Requirement

Running the scenario require the following software to be installed on your local computer:

1. `JDK 11`
2. `curl` utility

## 1.3. RabbitMQ on Pulsar

[Starlight for RabbitMQ (S4R)](https://docs.datastax.com/en/streaming/astra-streaming/developing/astream-rabbit.html) brings native RabbitMQ protocol support to Apache Pulsar, enabling migration of existing RabbitMQ applications and services to Pulsar without modifying the code.  It implements the AMQP 0.9.1 protocol used by RabbitMQ clients and translates AMQP frames and concepts to Pulsar ones.  S4R can be implemented in the following methods:

* Standalone Proxy between your RabbitMQ clients and Pulsar
* Plugin protocol handler to Pulsar 
* Pulsar Proxy extension

For details on Starlight for RabbitMQ (S4R) implementation, see docs at: https://github.com/datastax/starlight-for-rabbitmq

Please **NOTE** for this scenario, you will need access to:
* Pulsar instance running with a Starlight for RabbitMQ implementation as stated above 
**OR**
* An Astra Streaming instance with RabbitMQ enabled.  It supports [S4R out-of-the-box](https://docs.datastax.com/en/streaming/astra-streaming/developing/astream-rabbit.html#starlight-for-rabbitmq-quickstart).  No setup required.


In Pulsar, a RabbitMQ queue is backed by a Pulsar topic. Therefore, running this scenario requires a default Pulsar tenant, namespace, and topic.  For example:

* **tenant**: `mys4r`
* **namespace**: `rabbitmq`
* **topics**:
   * `mys4r/rabbitmq/s4rqueue`

Please **NOTE** that the creation of the above Pulsar "resources" can be **automated** by using the `deploy.sh` scrip. (see [Chapter 4](#4-deploy-the-scenario))

### 1.3.1. Customize the RabbitMQ Queue

If you want to run this scenario against a different set of Pulsar tenant, namespace, and topics, it can also be achieved by using a more advanced functionality of the `deploy.sh` script, via a `deployment properties` file. The document of [Deploying the Scenario](Deploy.Scenario.md) provides more details of how to do so.

# 2. Connect to the Pulsar Cluster

The RabbitMQ producer and consumer client apps can get connection info for the target Pulsar cluster from configuration file.  For this scenario, the client apps expect ths following parameters in a configure file:

```
username:
password:
host:
port:
virtual_host:
amqp_URI: 
```

Both the RabbitMQ producer and consumer client apps get the connection info to the target Pulsar cluster from a `client.conf` or similar file as described in this [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client).

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 3. Deploy the Scenario

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
       -cc : (Required) Pulsar 'client.conf' file path.
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -dp : (Optional) 'deploy.properties' file path (default to '<SCENARIO_HOMEDIR>/deploy.properties').
```

An example of using this script to deploy the scenario is as below:

```
deploy.sh -cc /tmp/client.conf
```

# 4. Run the Scenario

After all Pulsar resources are deployed, we can run the S4R client applications included in this scenario.

## 4.1. Run the RabbitMQ S4R Queue Consumer Client App

The following script [`runConsumer.sh`](_bash/runConsumer.sh) is used to run the RabbitMQ S4R consumer client app that receives the messages from the RabbitMQ queue hosted on Pulsar.

```
Usage: runConsumer.sh [-h]
                      [-na]
                      -q <queue_name>
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -q  : (Required) The queue name to receive messages from.
       -n  : (Required) The number of messages to consume.
       -cc : (Required) RabbitMQ 'client.conf' file and path.
```

An example of using this script to consuming 100 messages is as below:

```
runConsumer.sh -cc /tmp/client.conf -n 100 -q mys4r/rabbitmq/s4rqueue
```

## 4.2. Run the RabbitMQ S4R Queue Producer Client App

The following script [`runProducer.sh`](_bash/runProducer.sh) is used to run the RabbitMQ S4R producer client app.

```
Usage: runProducer.sh [-h]
                      [-na]
                      -q <queue_name>
                      -n <message_number>
                      -cc <client_conf_file>
       -h  : Show usage info
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -q  : (Required) The topic name to publish messages to.
       -n  : (Required) The number of messages to produce.
       -cc : (Required) RabbitMQ 'client.conf' file and path.
```

An example of using this script to publish 100 messages is as below:

```
runProducer.sh -cc /tmp/client.conf -n 100 -q mys4r/rabbitmq/s4rqueue
```