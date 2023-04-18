- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Scenario Programs](#11-scenario-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
  - [2.1. Pulsar Topic Schema](#21-pulsar-topic-schema)
- [3. Execution Steps](#3-execution-steps)
- [4. Verify the Results](#4-verify-the-results)

---

# 1. Scenario Overview

| | |
| - | - |
| **Name** |message-enrichment |
| **Description** | <ul><li>This scenario shows how to use a Pulsar function to enrich Pulsar messages that are published by a producer to a Pulsar topic.</li> <li>The enriched messages are sent by the function to another topic with a consumer subscribed to it for message consumption.</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer] -> (raw topic) -> [Pulsar Function] -> (processed topic) -> [Pulsar Consumer] |

## 1.1. Scenario Programs

There are 3 programs used in this scenario to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Type | Source Code | Description |
| ---- | ---- | ----------- | ----------- |
| IoTSensorProducer | Pulsar client app | [IotSensorProducer.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorProducer.java) | A Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic. |
| AddMetadataFunc | Pulsar function | [AddMetadataFunc.java](./function/src/main/java/com/example/pulsarworkshop/AddMetadataFunc.java) | A Pulsar function that adds a metadata property to each message of one topic and publishes a new message to another topic for further processing. |
| IoTSensorConsumer | Pulsar client app | [IotSensorConsumer.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorConsumer.java) | A standard Pulsar consumer client app that consumes from a topic that contains the processed messages with the new metadata property information. |

# 2. Deploy Pulsar Resources

In this scenario, the following default Pulsar resources need to be deployed first before running the scenario:

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topic**:
   * `msgenrich/testns/raw`
   * `msgenrich/testns/processed`
* **functions**:
  * `msgenrich/testns/add-metadata`.
     * It takes topic `msgenrich/testns/raw` as the input topic
     * It takes topic `msgenrich/testns/processed` as the output topic

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` scrip. (see [Deploy a Scenario](../../../Deploy.Scenario.md) document for more details)

## 2.1. Pulsar Topic Schema

In this scenario, both topics `msgenrich/testns/raw_a` and `msgenrich/testns/processed` use `STRING` as the message schema that represents the IoT Sensor Reading data.

# 3. Execution Steps

Let's assume the Pulsar cluster connection information is provided via the following file: `/tmp/client.conf`.

1. Deploy the scenario specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a Pulsar message consumer and wait for consuming messages from a Pulsar topic, `msgenrich/testns/processed`
```
runConsumer.sh -cc /tmp/client.conf -n 2 -t msgenrich/testns/processed
```

The received messages will be recorded in an application log file named `native-pulsar-IoTSensorConsumer-YYYMMDD.log`. This log file is created in the current folder where the `runConsumer.sh` script is executed. An example of two outputs of this log file is as below: 

```
20:50:42.613 [main] INFO  c.e.pulsarworkshop.IoTSensorConsumer - Starting application: "IoTSensorConsumer" ...
20:50:53.633 [main] INFO  c.e.pulsarworkshop.IoTSensorConsumer - (07665) Message received and acknowledged: key=null; properties={internal_process_time=2023-50-18 01:50:53.666}; value="1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
20:50:53.653 [main] INFO  c.e.pulsarworkshop.IoTSensorConsumer - (07665) Message received and acknowledged: key=null; properties={internal_process_time=2023-50-18 01:50:53.708}; value="1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
20:50:55.788 [main] INFO  c.e.pulsarworkshop.IoTSensorConsumer - Terminating application: "IoTSensorConsumer" ...
```

3. Start a Pulsar message producer and publishes messages to a Pulsar topic, `msgenrich/testns/raw`
```
runProducer.sh -cc /tmp/client.conf -n 2 -t msgenrich/testns/raw
```

The messages published will be recorded in an application log file named as `native-pulsar-IoTSensorProducer-YYYMMDD.log`. This log file is created in the current folder where the `runProducer.sh` script is executed. An example of two outputs of this log file is as below: 

```
20:50:51.830 [main] INFO  c.e.pulsarworkshop.IoTSensorProducer - Starting application: "IoTSensorProducer" ...
20:50:53.611 [main] INFO  c.e.pulsarworkshop.IoTSensorProducer - Published a message with raw value: [0] "1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
20:50:53.645 [main] INFO  c.e.pulsarworkshop.IoTSensorProducer - Published a message with raw value: [1] "1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
20:50:55.786 [main] INFO  c.e.pulsarworkshop.IoTSensorProducer - Terminating application: "IoTSensorProducer" ...
```

# 4. Verify the Results

According to the data flow pattern, each message received by the consumer client should have the following characteristics:
* The message payload is exactly the same as one raw record of the IoT sensor data which is read by the producer one by one from the source data file
* The message has a property named `internal_process_time` that is added by the Pulsar function `add-metadata`.

Comparing the consumer and producer result above, we can see that the above consumer message pattern is satisfied.