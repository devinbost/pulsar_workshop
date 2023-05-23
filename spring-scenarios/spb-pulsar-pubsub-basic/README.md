- [1. Demo Overview](#1-demo-overview)
  - [1.1. Demo Programs](#11-demo-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
  - [2.1. Pulsar Topic Schema](#21-pulsar-topic-schema)
- [3. Execution Steps](#3-execution-steps)
- [4. Verify the Results](#4-verify-the-results)

---

# 1. Demo Overview

| | |
| - | - |
| **Name** | spb-pulsar-pubsub-basic |
| **Description** | <ul><li>This demo shows how to the Spring Boot for Pulsar framework to publish messages to and consume messages from a Pulsar topic.</li> <li>The raw data source is a CSV file that includes actual readings in a particular time range from a given set of IoT sensors.</li></ul> |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [Pulsar Producer (Spring Boot)] -> (Pulsar topic) -> [Pulsar Consumer (Spring Boot)] |

## 1.1. Demo Programs

There are 2 programs used in this demo to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Source Code | Description |
| ---- | ----------- | ----------- |
| IoTSensorSpbpProducer | [IoTSensorSpbpProducer.java](./spb-pulsar-producer/src/main/java/com/example/pulsarworkshop/IoTSensorSpbpProducer.java) | A Spring Boot Pulsar producer client app that reads data from an IoT reading data source file (csv format) and publishes the data into a Pulsar topic. |
| IoTSensorSpbpConsumer | [IoTSensorSpbpConsumer.java](./spb-pulsar-consumer/src/main/java/com/example/pulsarworkshop/IoTSensorSpbpConsumer.java) | A Spring Boot Pulsar consumer client app that consumes from a topic that contains the IoT sensor reading messages. |

# 2. Deploy Pulsar Resources

In this demo, the following default Pulsar resources need to be deployed first before running the demo:

* **tenant**: `springtest`
* **namespace**: `default`
* **topic**: `spbp-pubsub-test`

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` scrip. (see [Deploy Demos](../../Deploy.Demos.md) document for more details)

## 2.1. Pulsar Topic Schema

In this demo, the Pulsar topic `springtest/default/spbp-pubsub-test` use the Apache AVRO schema that represents the IoT Sensor Reading data.
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

1. Deploy the demo specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a Pulsar message consumer and wait for consuming messages from a Pulsar topic, `springtest/default/spbp-pubsub-test`
```
runConsumer.sh -cc /tmp/client.conf -n 2 -t springtest/default/spbp-pubsub-test
```

The received messages will be recorded in an application log file named `spring-boot-pulsar-IoTSensorSpbpConsumer-YYYMMDD.log`. This log file is created in the current folder where the `runConsumer.sh` script is executed. An example of two outputs of this log file is as below: 

```
12:25:27.103 [main] INFO  c.e.p.IoTSensorSpbpConsumer - Starting Spring boot Pulsar consumer application 'IoTSensorSpbpConsumer'
12:25:27.524 [main] INFO  c.e.p.IoTSensorSpbpConsumer - Starting IoTSensorSpbpConsumer v1.0.0 using Java 17.0.7 with PID 82149 (/Users/yabinmeng/MyFolder/Yabin.Work/PSA.Vanguard/pulsar_workshop/spring-scenarios/spb-pulsar-pubsub-basic/spb-pulsar-consumer/target/spb-pulsar-consumer-1.0.0.jar started by yabinmeng in /Users/yabinmeng/MyFolder/Yabin.Work/PSA.Vanguard/pulsar_workshop/spring-scenarios/spb-pulsar-pubsub-basic)
12:25:27.525 [main] INFO  c.e.p.IoTSensorSpbpConsumer - No active profile set, falling back to 1 default profile: "default"
12:25:30.134 [main] INFO  c.e.p.IoTSensorSpbpConsumer - Started IoTSensorSpbpConsumer in 2.944 seconds (process running for 3.326)
12:25:42.117 [org.springframework.Pulsar.PulsarListenerEndpointContainer#0-0-C-1] INFO  c.e.p.IoTSensorSpbpConsumer - Successfully received message: msg-payload=IoTSensorData(ts=1.5945120943859746E9, device=b8:27:eb:bf:9d:51, co=0.004955938648391245, humidity=51.0, light=false, lpg=0.00765082227055719, motion=false, smoke=0.02041127012241292, temp=22.7))
12:25:42.125 [org.springframework.Pulsar.PulsarListenerEndpointContainer#0-0-C-1] INFO  c.e.p.IoTSensorSpbpConsumer - Successfully received message: msg-payload=IoTSensorData(ts=1.5945120943859746E9, device=b8:27:eb:bf:9d:51, co=0.004955938648391245, humidity=51.0, light=false, lpg=0.00765082227055719, motion=false, smoke=0.02041127012241292, temp=22.7))
12:25:42.125 [org.springframework.Pulsar.PulsarListenerEndpointContainer#0-0-C-1] INFO  c.e.p.IoTSensorSpbpConsumer - Successfully received message: msg-payload=IoTSensorData(ts=1.5945120943859746E9, device=b8:27:eb:bf:9d:51, co=0.004955938648391245, humidity=51.0, light=false, lpg=0.00765082227055719, motion=false, smoke=0.02041127012241292, temp=22.7))
... ...
```

3. Start a Pulsar message producer and publishes messages to a Pulsar topic, `springtest/default/spbp-pubsub-test`
```
runProducer.sh -cc /tmp/client.conf -n 2 -t springtest/default/spbp-pubsub-test
```

The messages published will be recorded in an application log file named as `spring-boot-pulsar-IoTSensorSpbpProducer-YYYMMDD.log`. This log file is created in the current folder where the `runProducer.sh` script is executed. An example of two outputs of this log file is as below: 

```
12:25:38.247 [main] INFO  c.e.p.IoTSensorSpbpProducer - Starting Spring boot Pulsar producer application 'IoTSensorSpbpProducer'
12:25:38.699 [main] INFO  c.e.p.IoTSensorSpbpProducer - Starting IoTSensorSpbpProducer v1.0.0 using Java 17.0.7 with PID 82302 (/Users/yabinmeng/MyFolder/Yabin.Work/PSA.Vanguard/pulsar_workshop/spring-scenarios/spb-pulsar-pubsub-basic/spb-pulsar-producer/target/spb-pulsar-producer-1.0.0.jar started by yabinmeng in /Users/yabinmeng/MyFolder/Yabin.Work/PSA.Vanguard/pulsar_workshop/spring-scenarios/spb-pulsar-pubsub-basic)
12:25:38.700 [main] INFO  c.e.p.IoTSensorSpbpProducer - No active profile set, falling back to 1 default profile: "default"
12:25:40.224 [main] INFO  c.e.p.IoTSensorSpbpProducer - Started IoTSensorSpbpProducer in 1.872 seconds (process running for 2.295)
12:25:41.942 [main] INFO  c.e.p.IoTSensorSpbpProducer - Successfully sent message: 756963:12:4 msg-payload=IoTSensorData(ts=1.5945120943859746E9, device=b8:27:eb:bf:9d:51, co=0.004955938648391245, humidity=51.0, light=false, lpg=0.00765082227055719, motion=false, smoke=0.02041127012241292, temp=22.7))
12:25:41.999 [main] INFO  c.e.p.IoTSensorSpbpProducer - Successfully sent message: 756966:13:2 msg-payload=IoTSensorData(ts=1.5945120947355676E9, device=00:0f:00:70:91:0a, co=0.0028400886071015706, humidity=76.0, light=false, lpg=0.005114383400977071, motion=false, smoke=0.013274836704851536, temp=19.700000762939453))
12:25:42.040 [main] INFO  c.e.p.IoTSensorSpbpProducer - Successfully sent message: 756966:14:2 msg-payload=IoTSensorData(ts=1.5945120980735729E9, device=b8:27:eb:bf:9d:51, co=0.004976012340421658, humidity=50.9, light=false, lpg=0.007673227406398091, motion=false, smoke=0.02047512557617824, temp=22.6))
... ...
```

# 4. Verify the Results

The Pulsar consumer client application receives the same IoT sensor data that are published by the Pulsar producer client application.