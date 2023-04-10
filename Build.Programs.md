
# 1. Build the Scenario Programs

The scenario programs included in this GitHub repository may be written in different languages such as Java, Python, Go, etc. The building process of these programs may vary from language to language.

## 1.1. Build Java Programs

All Java based programs in this GitHub repository are organized in a single [`Apache Maven`](https://maven.apache.org/) project in a structure like below:
```
scenarios
  |--- base-code
  |--- native-pulsar
  |      |--- <scenario_1_main_module>
  |             |--- <scenario_1_sub_module_1>
  |             |--- <scenario_1_sub_module_2>
  |             |--- ... ...
  |      |--- <scenario_2_main_module>
  |             |--- ... ...
  |--- jms-s4j
  |      |--- ... ...
  |--- kafka-s4k
  |      |--- ... ...
  |--- rabbitmq-s4r
  |      |--- ... ...
  |--- spring-pulsar
  |      |--- ... ...
  |--- ... ...
``` 

In the above structure,
* The `scenarios` module is the "root" parent module for all other modules
* The `base-code` module contains some common codes that are used by scenario specific modules.
* The scenario specific modules are further organized by the APIs that scenario programs are using
   * Native Pulsar API
   * Starlight for JMS API
   * Starlight for Kafka API
   * Starlight for RabbitMQ API
   * Spring Boot Pulsar API
* Each scenario specific module may or may not have its own submodules.

In order to build all scenarios, go to `scenarios` folder and run the following command:
```
mvn clean install
```