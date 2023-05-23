- [1. Demo Overview](#1-demo-overview)
  - [1.1. Demo Programs](#11-demo-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
  - [2.1. Pulsar Topic Schema](#21-pulsar-topic-schema)
- [3. Execution Steps](#3-execution-steps)
- [5. Verify the Results](#5-verify-the-results)

---

# 1. Demo Overview

| | |
| - | - |
| **Name** | message-redlivery |
| **Description** | <ul><li>This demo shows how to configure a Pulsar consumer for message redelivery and how to handle a message once it's been redelivered the maximum times allowed by the configuration .</li> <li>Messages are produced by a standard producer and consumed by a consumer that will negative acknowledge the message triggering a redelivery attempt.  The consumer will try five times and then publish the failed message to the configured dead letter topic</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.  This demo will only use the first record in the file</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer] -> (topic) -> [Pulsar Consumer] -> (dead letter topic) -> [Dead Letter Pulsar Consumer] |

## 1.1. Demo Programs

There are 3 programs used in this demo to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Type | Source Code | Description |
| ---- | ---- | ----------- | ----------- |
| SimpleProducer | Pulsar client app | [SimpleProducer.java](./client-app/src/main/java/com/example/pulsarworkshop/SimpleProducer.java) | A Pulsar producer client app that reads a row from an IoT data source file (csv format) and publishes the data into a Pulsar topic. |
| RedeliveryConsumer | Pulsar client app | [RedeliveryConsumer.java](./function/src/main/java/com/example/pulsarworkshop/RedeliveryConsumer.java) | A Pulsar client app that attempts to consume the message a maximum of 5 times before it publishes the message to a dead letter topic. |
| DeadLetterConsumer | Pulsar client app | [DeadLetterConsumer.java](./client-app/src/main/java/com/example/pulsarworkshop/DeadLetterConsumer.java) | A standard Pulsar consumer client app that consumes from the dead letter topic that contains the message that failed to be consumed by the RedeliveryConsumer. |

# 2. Deploy Pulsar Resources

In this demo, the following default Pulsar resources need to be deployed first before running the demo:

* **tenant**: `redelivery-demo`
* **namespace**: `demo`
* **topic**:
   * `redelivery-demo/demo/target-topic`
   * `redelivery-demo/demo/dlt-topic`

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` script. (see [Deploy Demos](../../../Deploy.Demos.md) document for more details)

## 2.1. Pulsar Topic Schema

In this demo, both topics `redelivery-demo/demo/dlt-topic` and `redelivery-demo/demo/target-topic` use `STRING` as the message schema that represents the IoT Sensor Reading data.

# 3. Execution Steps

Let's assume the Pulsar cluster connection information is provided via the following file: `/tmp/client.conf`.

1. Deploy the demo specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a Pulsar message consumer and wait for consuming messages from the target topic, `redelivery-demo/demo/target-topic`
```
runConsumer.sh -cc /tmp/client.conf -n 2 -t redelivery-demo/demo/target-topic -c redelivery
```
3. Start a Pulsar message consumer and wait for consuming messages from a the dead letter topic, `redelivery-demo/demo/dlt-topic`
```
runConsumer.sh -cc /tmp/client.conf -n 2 -t redelivery-demo/demo/dlt-topic -c dlt
```
4. Start a Pulsar message producer and publishes messages to a Pulsar topic, `redelivery-demo/demo/target-topic`
```
runProducer.sh -cc /tmp/client.conf -n 2 -t redelivery-demo/demo/target-topic
```
# 5. Verify the Results

The workflow for this demo is as follows:
* The `SimpleProducer` will deliver one message to the primary topic `redelivery-demo/demo/target-topic`.  
* Once that message is published, the `RedeliveryConsumer` will consume the message and negative acknowledge it 5 times.  
  * Logs for this consumer should show each of the five attempts to process the message
* On the fifth attempt, the message will be published to the dead letter topic `redelivery-demo/demo/dlt-topic`.
The message will be consumed by the `DeadLetterConsumer`.
  * Logs for this consumer should show that the message was processed successfully.
* The message payload is exactly the same as one raw record of the IoT sensor data which is read by the producer one by one from the source data file

Comparing the consumer and producer result above, we can see that the message redelivery and dead letter topic consumer pattern is satisfied.