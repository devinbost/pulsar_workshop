# Overview

The goal of this GitHub repository is to demonstrate with concrete code examples of how Apache Pulsar can be used as a powerful and unified platform for common messaging and streaming processing use cases. 

This repository is composed of a series of `demo scenarios` (or simply `scenarios`). Each **scenario** is a self-contained unit that covers a complete, end-to-end messaging/streaming processing use case using a specific message processing protocol that is supported by Pulsar.

# Scenario List

The table below lists all scenarios that are available in this repository.

| API | Scenario Name | Description |
| --- | ------------- | ----------- |
| Native Pulsar | [native-pulsar/message-enrichment](scenarios/native-pulsar/message-enrichment/README.md) | Demonstrate how an IoT sensor reading raw data can be published and enriched in Pulsar before consumed by a consumer. |
| Native Pulsar | [native-pulsar/message-enrichment-avro](scenarios/native-pulsar/message-enrichment-avro/README.md) | Similar to the `message-enrichment` scenario. The only difference is this scenario demonstrates the message processing with AVRO/JSON schema instead of as pure text. |
| Starlight for JMS | [jms-s4j/p2p-basic](scenarios/jms-s4j/p2p-basic/README.md) | Demonstrate JMS message sending and receiving with a JMS Queue using S4J API and Pulsar |
| Starlight for JMS | [jms-s4j/pubsub-basic](scenarios/jms-s4j/pubsub-basic/README.md) | Demonstrate JMS message publishing and subscribing with a JMS Topic using S4J API and Pulsar |