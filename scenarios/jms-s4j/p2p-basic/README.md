- [1. Scenario Overview](#1-scenario-overview)
  - [1.1. Scenario Programs](#11-scenario-programs)
- [2. Deploy Pulsar Resources](#2-deploy-pulsar-resources)
- [3. Execution Steps](#3-execution-steps)
- [4. Verify the Results](#4-verify-the-results)

---

# 1. Scenario Overview

| | |
| - | - |
| **Name** | JMS+S4J p2p-basic |
| **Description** | This scenario shows how to use the Starlight for JMS (S4J) API with Pulsar to do native message sending and receiving with a **JMS queue**. |
| **Data Flow Pattern** | <IoT_sensor_reading_data> -> [JMS Queue Sender] -> (JMS queue) -> [JMS Queue Receiver] |

## 1.1. Scenario Programs

There are 2 programs used in this scenario to demonstrate the end-to-end data flow pattern. All these programs are written in **Java**. 

| Name | Source Code | Description |
| ---- | ----------- | ----------- |
| IoTSensorQueueSender | [IoTSensorQueueSender.java](./src/main/java/com/example/pulsarworkshop/IoTSensorQueueSender.java) | A JMS sender client app that reads data from an IoT reading data source file (CSV format) and sends the data to a JMS queue which is backed by a Pulsar topic behind the scene. |
| IoTSensorQueueReceiver | [IoTSensorQueueReceiver.java](./src/main/java/com/example/pulsarworkshop/IoTSensorQueueReceiver.java) | A JMS receiver client app that receives messages from a JMS queue which is backed by a Pulsar topic behind the scene. |

# 2. Deploy Pulsar Resources

In Pulsar, a JMS queue is backed by a Pulsar topic. In this scenario, the following default Pulsar resources need to be deployed first before running the scenario: 

* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/s4j_p2p`

Please **NOTE** that the creation of the above Pulsar resources is done via the `deploy.sh` scrip. (see [Deploy a Scenario](../../../Deploy.Scenario.md) document for more details)

# 3. Execution Steps

Let's assume the Pulsar cluster connection information is provided via the following file: `/tmp/client.conf`.

1. Deploy the scenario specific resources
```
deploy.sh -cc /tmp/client.conf
```

2. Start a JMS message receiver and wait for consuming messages from a JMS Queue that is backed by the Pulsar topic `msgenrich/testns/s4j_p2p`
```
runConsumer.sh -cc /tmp/client.conf -n 2 -t msgenrich/testns/s4j_p2p
```

The received messages will be recorded in an application log file named `jms-s4j-IoTSensorQueueReceiver-YYYMMDD.log`. This log file is created in the current folder where the `runConsumer.sh` script is executed. An example of two outputs of this log file is as below:  

```
21:06:19.823 [main] INFO  c.e.p.IoTSensorQueueReceiver - Starting application: "IoTSensorQueueReceiver" ...
21:06:51.353 [main] INFO  c.e.p.IoTSensorQueueReceiver - Message received from topic persistent://msgenrich/testns/s4j_p2p: value="1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
21:06:51.371 [main] INFO  c.e.p.IoTSensorQueueReceiver - Message received from topic persistent://msgenrich/testns/s4j_p2p: value="1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
21:06:53.513 [main] INFO  c.e.p.IoTSensorQueueReceiver - Terminating application: "IoTSensorQueueReceiver" ...
```

3. Start a JMS message sender and publishes messages to a JMS Queue that is backed by the Pulsar topic `msgenrich/testns/s4j_p2p`
```
runProducer.sh -cc /tmp/client.conf -n 2 -t msgenrich/testns/s4j_p2p
```

The messages published will be recorded in an application log file named as `jms-s4j-IoTSensorQueueSender-YYYMMDD.log`. This log file is created in the current folder where the `runProducer.sh` script is executed. An example of two outputs of this log file is as below: 

```
21:06:49.508 [main] INFO  c.e.p.IoTSensorQueueSender - Starting application: "IoTSensorQueueSender" ...
21:06:51.332 [main] INFO  c.e.p.IoTSensorQueueSender - IoT sensor data sent to queue persistent://msgenrich/testns/s4j_p2p [0] "1.5945120943859746E9","b8:27:eb:bf:9d:51","0.004955938648391245","51.0","false","0.00765082227055719","false","0.02041127012241292","22.7"
21:06:51.367 [main] INFO  c.e.p.IoTSensorQueueSender - IoT sensor data sent to queue persistent://msgenrich/testns/s4j_p2p [1] "1.5945120947355676E9","00:0f:00:70:91:0a","0.0028400886071015706","76.0","false","0.005114383400977071","false","0.013274836704851536","19.700000762939453"
21:06:53.496 [main] INFO  c.e.p.IoTSensorQueueSender - Terminating application: "IoTSensorQueueSender" ...
```

# 4. Verify the Results

This is a simple Kafka producer and consumer scenario without any extra message processing. The main purpose of this scenario is to demonstrate how to use Apache Pulsar as a drop-in replace of a JMS broker and serve native JMS client applications with JMS topics with no code change. 

The JMS consumer client application receives exactly the same IoT sensor data that are published by the JMS producer client application.