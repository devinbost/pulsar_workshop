# Overview

The goal of this GitHub repository is to demonstrate with concrete code examples of how Apache Pulsar can be used as a powerful and unified platform for common messaging and streaming processing use cases. 


This repository is composed of a series of `demo scenario`s (or simply `scenario`s). Each **scenario** is a self-contained unit that covers a complete, end-to-end messaging/streaming processing use case.

# Scenario List

The table below lists all scenarios that are available in this repository.

| Scenario Name | Description |
| ------------- | ----------- |
| [message-enrichment](scenarios/message-enrichment) | Demonstrate how an IoT sensor reading raw data can be published and enriched in Pulsar before consumed by a consumer. |
| [message-enrichment-avro](scenarios/message-enrichment-avro) | Similar to the `message-enrichment` scenario. The only difference is this scenario demonstrates the message processing with AVRO/JSON schema instead of as pure text. |
| [schema-compatibility (TBD)](scenarios/schema-compatibility) | Demonstrate how Pulsar message schema compatibility is handled. |