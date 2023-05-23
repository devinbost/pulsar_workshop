- [1. Overview](#1-overview)
  - [1.1. Software Requirement](#11-software-requirement)
  - [1.2. Build Demo Programs](#12-build-demo-programs)
  - [1.3. Deploy and Run the Demos](#13-deploy-and-run-the-demos)
- [2. Demo List](#2-demo-list)
  - [2.1. Demos under `scenarios` subfolder](#21-demos-under-scenarios-subfolder)
  - [2.2. Demos under `spring-scenarios` subfolder](#22-demos-under-spring-scenarios-subfolder)

---

# 1. Overview

The goal of this GitHub repository is to demonstrate with concrete code examples of how Apache Pulsar can be used as a powerful and unified platform for common messaging and streaming processing use cases. 

This repository is composed of a series of `demo scenarios` (or simply `demos`). Each **demo** is a self-contained unit that covers a complete, end-to-end messaging/streaming processing use case using a specific message processing protocol that is supported by Pulsar. 

The majority of the demos are organized under the subfolder of `scenarios`, and they're then further grouped into following categories based on the underlying API and/or messaging protocols being used.
* Native Pulsar API and protocol (`native-pulsar`)
* JMS specification with Starlight for JMS (S4J) API (`jms-s4j`)
* Kafka protocol with Starlight for Kafka (S4J) protocol handler (`kafka-s4k`)
* AMQP protocol with Starlight for RabbitMQ (S4R) protocol handler (`rabbitmq-s4r`)

There are also a subset of the demos that are organized under another subfolder of `spring-scenarios`. These demos are written using the Java Spring Boot framework for Pulsar. Because this framework requires JDK17 as the minimum version requirement. This is different from the rest of the demos that run at JDK11. (Please see [Build. Programs](./Build.Programs.md) for more details)

## 1.1. Software Requirement

Running the scenario require the following software to be installed on your local computer:

1. `JDK 11` (for `scenarios`) and `JDK17` (for `spring-scenarios`) 
2. `curl` utility

## 1.2. Build Demo Programs

Each demo has its own set of programs to showcase how message publishing/producing/sending or subscribing/consuming/receiving works in that particular demo. In order to run these demos, we first need to build their programs Please refer to the document of [Build Scenario Programs](./Build.Programs.md) for more details.

## 1.3. Deploy and Run the Demos 

Each of the demos included in this repository has 3 standard bash scripts that can be found under the `_bash` sub-folder of each demo.

* **`deploy.sh`** is used to deploy the Pulsar resources (tenant, namespace, topic, function, etc.) as needed in the demo.
* **`runProducer.sh`** is used to publish/send/produce messages to one Pulsar topic
* **`runConsumer.sh`** is used to subscribe/receive/consume messages from one Pulsar topic

We first need to run `deploy.sh` in order to deploy the Pulsar resources (tenant, namespace, topic, function, etc.) that are needed for a particular demo. The `deploy.sh` has the standard input command line input parameters; but they will deploy different sets of Pulsar resources that are unique to each demo. For more details, please see the document of [Deploy Demos](./Deploy.Demos.md).

After that, we can either run `runProducer.sh` to publish/produce/send messages or run `runConsumer.sh` to subscribe/consume/receive messages. These 2 scripts take some common command line input parameters but may be slightly different from demo to demo. For the general description of running a demo, please see the document of [Run Demos](./Run.Demos.md). The demo specific execution steps will be explained in details under each demo.

# 2. Demo List

## 2.1. Demos under `scenarios` subfolder

| API | Demo Name | Description |
| --- | --------- | ----------- |
| Native Pulsar | [native-pulsar/message-enrichment](scenarios/native-pulsar/message-enrichment/README.md) | Demonstrate how an IoT sensor reading raw data can be published and enriched in Pulsar before consumed by a consumer. |
| Native Pulsar | [native-pulsar/message-enrichment-avro](scenarios/native-pulsar/message-enrichment-avro/README.md) | Similar to the `message-enrichment` scenario. The only difference is this scenario demonstrates the message processing with AVRO/JSON schema instead of as pure text. |
| JMS + S4J | [jms-s4j/p2p-basic](scenarios/jms-s4j/p2p-basic/README.md) | Demonstrate basic JMS message sending and receiving with a JMS Queue using S4J API and Pulsar |
| JMS + S4J | [jms-s4j/pubsub-basic](scenarios/jms-s4j/pubsub-basic/README.md) | Demonstrate basic JMS message publishing and subscribing with a JMS Topic using S4J API for Pulsar |
| Kafka + S4K | [kafka-s4k/pubsub-basic](scenarios/kafka-s4k/pubsub-basic/README.md) | Demonstrate basic Kafka message producing and consuming with a Kafka Topic with Pulsar's S4K protocol handler |
| RabbitMQ + S4R | [rabbitmq-s4r/pubsub-queue](scenarios/rabbitmq-s4r/pubsub-queue/README.md) | Demonstrate RabbitMQ message producing and consuming with a RabbitMQ Queue using S4R API and Pulsar |

## 2.2. Demos under `spring-scenarios` subfolder

| Spring Boot Demo Name | Description |
| --------------------- | ----------- |
[spb-pulsar-pubsub-basic](spring-scenarios/spb-pulsar-pubsub-basic/README.md) | Demonstrate basic message publishing and consuming using Spring boot for Pulsar framework |