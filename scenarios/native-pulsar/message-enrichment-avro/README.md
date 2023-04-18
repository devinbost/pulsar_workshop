- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Scenario Programs](#11-scenario-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
  - [2.1. Pulsar Topic Schema](#21-pulsar-topic-schema)
- [3. Execution Steps](#3-execution-steps)
- [4. Verify the Results](#4-verify-the-results)

---

# 1. Scenario Overview

Function wise this scenario, `message-enrichment-avro` is exactly the same as the [`message-enrichment`](../message-enrichment/) scenario in which the following end-to-end data processing flow is demonstrated:
```
<IoT_sensor_reading_data> -> [Pulsar Producer] -> (raw topic) -> [Pulsar Function] -> (processed topic) -> [Pulsar Consumer]
```

The **ONLY** difference between this scenario and the `message-enrichment` scenario is:
* In `message-enrichment` scenario, all messages are processed as simple strings. The corresponding topics have `STRING` as the message schema.
* In this scenario, all messages are processed as Apache AVRO records. The corresponding topics have `AVRO` as the message schema.

## 1.1. Scenario Programs

There are 3 programs used in this scenario to demonstrate the end-to-end data flow pattern with Apache AVRO as the message schema for all topics involved in the processing. All these programs are written in **Java**. 

| Name | Type | Source Code | Description |
| ---- | ---- | ----------- | ----------- |
| IoTSensorProducerAvro | Pulsar client app | [IotSensorProducerAvro.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorProducerAvro.java) | A Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic. |
| AddMetadataFuncAvro | Pulsar function | [AddMetadataFuncAvro.java](./function/src/main/java/com/example/pulsarworkshop/AddMetadataFuncAvro.java) | A Pulsar function that adds a metadata property to each message of one topic and publishes a new message to another topic for further processing. |
| IoTSensorConsumerAvro | Pulsar client app | [IotSensorConsumerAvro.java](./client-app/src/main/java/com/example/pulsarworkshop/IoTSensorConsumerAvro.java) | A standard Pulsar consumer client app that consumes from a topic that contains the processed messages with the new metadata property information. |

# 2. Deploy Pulsar Resources

By default, running this scenario requires the following Pulsar tenant, namespace, and topics. 

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/raw_a`
   * `msgenrich/testns/processed_a`
* **functions**:
  * `msgenrich/testns/add-metadata`.
     * It takes topic `msgenrich/testns/raw_a` as the input topic
     * It takes topic `msgenrich/testns/processed_a` as the output topic

Please **NOTE** that the creation of the above Pulsar "resources" can be **automated** by using the `deploy.sh` scrip. (see [Chapter 4](#4-deploy-the-scenario))

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` scrip. (see [Deploy a Scenario](../../../Deploy.Scenario.md) document for more details)

## 2.1. Pulsar Topic Schema

In this scenario, both topic `raw_a` and `processed_a` use the same Apache AVRO schema that represents the IoT Sensor Reading data.
```
{
  "type": "record",
  "name": "IoTSensorData",
  "namespace": "com.example.pulsarworkshop",
  "fields": [
    {
      "name": "co",
      "type": "double"
    },
    {
      "name": "device",
      "type": [
        "null",
        "string"
      ],
      "default": null
    },
    {
      "name": "humidity",
      "type": "double"
    },
    {
      "name": "light",
      "type": "boolean"
    },
    {
      "name": "lpg",
      "type": "double"
    },
    {
      "name": "motion",
      "type": "boolean"
    },
    {
      "name": "smoke",
      "type": "double"
    },
    {
      "name": "temp",
      "type": "double"
    },
    {
      "name": "ts",
      "type": [
        "null",
        "string"
      ],
      "default": null
    }
  ]
}
```

# 3. Execution Steps

Let's assume the Pulsar cluster connection information is provided via the following file: `/tmp/client.conf`.

1. Deploy the scenario specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a Pulsar message consumer and wait for consuming messages from a Pulsar topic, `msgenrich/testns/processed_a`
```
runConsumer.sh -cc /tmp/client.conf -n 10 -t msgenrich/testns/processed_a
```

The received messages will be recorded in an application log file named `native-pulsar-IoTSensorConsumerAvro-YYYMMDD.log`. This log file is created in the current folder where the `runConsumer.sh` script is executed. An example of two outputs of this log file is as below: 

```
20:35:24.383 [main] INFO  c.e.p.IoTSensorConsumerAvro - Starting application: "IoTSensorConsumerAvro" ...
20:35:31.360 [main] INFO  c.e.p.IoTSensorConsumerAvro - (2d7b0) Message received and acknowledged: key=null; properties={internal_process_time=2023-35-18 01:35:31.302}; value=IoTSensorData(ts=1.5945120943859746E9, device=b8:27:eb:bf:9d:51, co=0.004955938648391245, humidity=51.0, light=false, lpg=0.00765082227055719, motion=false, smoke=0.02041127012241292, temp=22.7)
20:35:31.372 [main] INFO  c.e.p.IoTSensorConsumerAvro - (2d7b0) Message received and acknowledged: key=null; properties={internal_process_time=2023-35-18 01:35:31.343}; value=IoTSensorData(ts=1.5945120947355676E9, device=00:0f:00:70:91:0a, co=0.0028400886071015706, humidity=76.0, light=false, lpg=0.005114383400977071, motion=false, smoke=0.013274836704851536, temp=19.700000762939453)
20:35:33.507 [main] INFO  c.e.p.IoTSensorConsumerAvro - Terminating application: "IoTSensorConsumerAvro" ...
```

3. Start a Pulsar message producer and publishes messages to a Pulsar topic, `msgenrich/testns/raw_a`
```
runProducer.sh -cc /tmp/client.conf -n 10 -t msgenrich/testns/raw_a
```

The messages published will be recorded in an application log file named as `native-pulsar-IoTSensorProducerAvro-YYYMMDD.log`. This log file is created in the current folder where the `runProducer.sh` script is executed. An example of two outputs of this log file is as below: 

```
20:35:29.519 [main] INFO  c.e.p.IoTSensorProducerAvro - Starting application: "IoTSensorProducerAvro" ...
20:35:31.226 [main] INFO  c.e.p.IoTSensorProducerAvro - Published a message with raw value: [0] "1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
20:35:31.268 [main] INFO  c.e.p.IoTSensorProducerAvro - Published a message with raw value: [1] "1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
20:35:33.407 [main] INFO  c.e.p.IoTSensorProducerAvro - Terminating application: "IoTSensorProducerAvro" ...
```

# 4. Verify the Results

According to the data flow pattern, each message received by the consumer client should have the following characteristics:
* The message payload is String that represents the AVRO object of one raw record of the IoT sensor data which is read by the producer one by one from the source data file.
* The message has a property named `internal_process_time` that is added by the Pulsar function `add-metadata-avro`.

Comparing the consumer and producer result above, we can see that the above consumer message pattern is satisfied.