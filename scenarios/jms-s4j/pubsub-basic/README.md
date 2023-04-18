- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Scenario Programs](#11-scenario-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
- [3. Execution Steps](#3-execution-steps)
- [4. Verify the Results](#4-verify-the-results)

---

# 1. Scenario Overview

| | |
| - | - |
| **Name** | JMS+S4J pubsub-basic |
| **Description** | This scenario shows how to use the Starlight for JMS (S4J) API with Pulsar to do native message publishing and subscribing with a **JMS topic**. |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [JMS Topic Publisher] -> (JMS topic) -> [JMS Topic Subscriber] |

## 1.1. Scenario Programs

There are 2 programs used in this scenario to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Source Code | Description |
| ---- | ----------- | ----------- |
| IoTSensorTopicSubscriber | [IoTSensorTopicSubscriber.java](./src/main/java/com/example/pulsarworkshop/IoTSensorTopicSubscriber.java) | A JMS publisher client app that reads data from an IoT reading data source file (CSV format) and publishes the data to a JMS topic which is backed by a Pulsar topic behind the scene. |
| IoTSensorTopicPublisher | [IoTSensorTopicPublisher.java](./src/main/java/com/example/pulsarworkshop/IoTSensorTopicPublisher.java) | A JMS subscriber client app that subscribes messages from a JMS topic which is backed by a Pulsar topic behind the scene. |

# 2. Deploy Pulsar Resources

In Pulsar, a JMS topic is backed by a Pulsar topic. In this scenario, the following default Pulsar resources need to be deployed first before running the scenario: 

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/s4j_pubsub`

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` scrip. (see [Deploy a Scenario](../../../Deploy.Scenario.md) document for more details)

# 3. Execution Steps

Let's assume the Pulsar cluster connection information is provided via the following file: `/tmp/client.conf`.

1. Deploy the scenario specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a JMS message subscriber and wait for consuming messages from a JMS Topic (10 messages as in the example below)
```
runConsumer.sh -cc /tmp/client.conf -n 10 -t msgenrich/testns/s4j_pubsub
```

The received messages will be recorded in an application log file named `jms-s4j-IoTSensorTopicSubscriber-YYYMMDD.log`. This log file is created in the current folder where the `runConsumer.sh` script is executed. An example of this log file is as below: 

```
21:22:01.751 [main] INFO  c.e.p.IoTSensorTopicSubscriber - Starting application: "IoTSensorTopicSubscriber" ...
21:22:16.474 [main] INFO  c.e.p.IoTSensorTopicSubscriber - Message received from topic persistent://msgenrich/testns/s4j_pubsub: value="1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
21:22:16.491 [main] INFO  c.e.p.IoTSensorTopicSubscriber - Message received from topic persistent://msgenrich/testns/s4j_pubsub: value="1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
21:22:18.632 [main] INFO  c.e.p.IoTSensorTopicSubscriber - Terminating application: "IoTSensorTopicSubscriber" ...
```

3. Start a JMS message publisher and publishes messages to a JMS Topic
```
runProducer.sh -cc /tmp/client.conf -n 10 -t msgenrich/testns/s4j_pubsub
```

The messages published will be recorded in an application log file named as `jms-s4j-IoTSensorTopicPublisher-YYYMMDD.log`. This log file is created in the current folder where the `runProducer.sh` script is executed. An example of this log file is as below: 

```
21:22:14.415 [main] INFO  c.e.p.IoTSensorTopicPublisher - Starting application: "IoTSensorTopicPublisher" ...
21:22:16.454 [main] INFO  c.e.p.IoTSensorTopicPublisher - IoT sensor data published to topic persistent://msgenrich/testns/s4j_pubsub [0] "1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
21:22:16.493 [main] INFO  c.e.p.IoTSensorTopicPublisher - IoT sensor data published to topic persistent://msgenrich/testns/s4j_pubsub [1] "1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
21:22:18.632 [main] INFO  c.e.p.IoTSensorTopicPublisher - Terminating application: "IoTSensorTopicPublisher" ...
```

# 4. Verify the Results

This is a simple Kafka producer and consumer scenario without any extra message processing. The main purpose of this scenario is to demonstrate how to use Apache Pulsar as a drop-in replace of a JMS broker and serve native JMS client applications with JMS queues with no code change. 

The JMS receiver client application receives exactly the same IoT sensor data that are published by the JMS publisher client application.