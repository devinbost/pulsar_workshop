- [1. Overview](#1-overview)
  - [1.1. Software Requirement](#11-software-requirement)
  - [1.2. Build Scenario Programs](#12-build-scenario-programs)
  - [1.3. Deploy and Run the Scenarios](#13-deploy-and-run-the-scenarios)
- [2. Scenario List](#2-scenario-list)

---

# 1. Overview

The goal of this GitHub repository is to demonstrate with concrete code examples of how Apache Pulsar can be used as a powerful and unified platform for common messaging and streaming processing use cases. 

This repository is composed of a series of `demo scenarios` (or simply `scenarios`). Each **scenario** is a self-contained unit that covers a complete, end-to-end messaging/streaming processing use case using a specific message processing protocol that is supported by Pulsar. 

These scenarios are organized at high level by the underlying API and/or messaging protocols being used. Right now, the scenarios are orgnized into the following categories:
* Native Pulsar API and protocol (`native-pulsar`)
* Spring Boot with Pulsar API (`spring-pulsar`)
* JMS specification with Starlight for JMS (S4J) API (`jms-s4j`)
* Kafka protocol with Starlight for Kafka (S4J) protocol handler (`kafka-s4k`)
* AMQP protocol with Starlight for RabbitMQ (S4R) protocol handler (`rabbitmq-s4r`)

## 1.1. Software Requirement

Running the scenario require the following software to be installed on your local computer:

1. `JDK 11`
2. `curl` utility

## 1.2. Build Scenario Programs

Each scenario has its own set of programs to showcase how message publishing/producing/sending or subscribing/consuming/receiving works in that particular scenario. In order to run these scenarios, we first need to build their programs Please refer to the document of [Build Scenario Programs](./Build.Programs.md) for more details.

## 1.3. Deploy and Run the Scenarios 

Each of the scenarios included in this repository has 3 standard bash scripts that can be found under the `_bash` sub-folder of each scenario.

* **`deploy.sh`** is used to deploy the Pulsar resources (tenant, namespace, topic, function, etc.) as needed in the scenario.
* **`runProducer.sh`** is used to publish/send/produce messages to one Pulsar topic
* **`runConsumer.sh`** is used to subscribe/receive/consume messages from one Pulsar topic

We first need to run `deploy.sh` in order to deploy the Pulsar resources (tenant, namespace, topic, function, etc.) that are needed for a particular scenario. The `deploy.sh` has the standard input command line input parameters; but they will deploy different sets of Pulsar resources that are unique to each scenario. For more details, please see the document of [Deploy a Scenario](./Deploy.Scenario.md).

After that, we can either run `runProducer.sh` to publish/produce/send messages or run `runConsumer.sh` to subscribe/consume/receive messages. These 2 scripts take some common command line input parameters but may be slightly different from scenario to scenario. For the general description of running a scenario, please see the document of [Run a Scenario](./Run.Scenario.md). The scenario specific execution steps will be explained in details under each scenario.

# 2. Scenario List

The table below lists all scenarios that are available in this repository.

| API | Scenario Name | Description |
| --- | ------------- | ----------- |
| Native Pulsar | [native-pulsar/message-enrichment](scenarios/native-pulsar/message-enrichment/README.md) | Demonstrate how an IoT sensor reading raw data can be published and enriched in Pulsar before consumed by a consumer. |
| Native Pulsar | [native-pulsar/message-enrichment-avro](scenarios/native-pulsar/message-enrichment-avro/README.md) | Similar to the `message-enrichment` scenario. The only difference is this scenario demonstrates the message processing with AVRO/JSON schema instead of as pure text. |
| JMS + S4J | [jms-s4j/p2p-basic](scenarios/jms-s4j/p2p-basic/README.md) | Demonstrate basic JMS message sending and receiving with a JMS Queue using S4J API and Pulsar |
| JMS + S4J | [jms-s4j/pubsub-basic](scenarios/jms-s4j/pubsub-basic/README.md) | Demonstrate basic JMS message publishing and subscribing with a JMS Topic using S4J API for Pulsar |
| Kafka + S4K | [kafka-s4k/pubsub-basic](scenarios/kafka-s4k/pubsub-basic/README.md) | Demonstrate basic Kafka message producing and consuming with a Kafka Topic with Pulsar's S4K protocol handler |
| RabbitMQ + S4R | [rabbitmq-s4r/pubsub-queue](scenarios/rabbitmq-s4r/pubsub-queue/README.md) | Demonstrate RabbitMQ message producing and consuming with a RabbitMQ Queue using S4R API and Pulsar |