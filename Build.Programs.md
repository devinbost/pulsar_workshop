- [1. Build the Demos](#1-build-the-demos)
  - [1.1. Build Java Programs](#11-build-java-programs)
  - [1.2. Manage Multiple Java Versions](#12-manage-multiple-java-versions)


---


# 1. Build the Demos

The demo programs included in this GitHub repository can be written in different languages such as Java, Python, Go, etc. The building process of these programs may vary from language to language.

## 1.1. Build Java Programs

All Java based programs in this GitHub repository are organized in two main [`Apache Maven`](https://maven.apache.org/) projects, `demo` and `spring-demos` in a structure like below. 

The only reason to use two separate Maven projects is that they require different Java versions:
1. `demo` project requires Java 11 and can't work with Java 17 because Java 17 based Pulsar function can't be deployed in Astra Streaming yet.
2. `spring-demos` project requires Java 17 and can't work with Java 11 because that is the [requirement](https://docs.spring.io/spring-pulsar/docs/current-SNAPSHOT/reference/html/#_minimum_supported_versions) of Spring boot Pulsar starter.

```
pulsar_workshop
├── demos
│   ├── base-code
│   ├── jms-s4j
│   │   ├── common-resources
│   │   ├── <jms_s4j_module_1>
│   │   ├── <jms_s4j_module_2>
│   │   └── ... ...
│   ├── kafka-s4k
│   │   ├── common-resources
│   │   ├── <kafka-s4k_module_1>
│   │   ├── <kafka-s4k_module_2>
│   │   └── ... ...
│   ├── native-pulsar
│   │   ├── common-resources
│   │   ├── <native-pulsar_module_1>
│   │   ├── <native-pulsar_module_2>
│   │   └── ... ...
│   └── rabbitmq-s4r
│       ├── <rabbitmq-s4r_module_1>
│       ├── <rabiitmq-s4r_module_2>
│       └── ... ...
└── spring-demos
    ├── spb-common-resources
    └── spb-pulsar-pubsub-basic
        ├── <spring-boot-pulsar-module_1>
        ├── <spring-boot-pulsar-module_2>        
        └── ... ...
``` 

Building these Maven projects is as simple as running the following maven command under the project home directory:
```
mvn clean install
```

## 1.2. Manage Multiple Java Versions

Since building this repo requires two different Java versions, it is recommended to use a multi-java-version management tool like [jEnv](https://github.com/jenv/jenv).

Based on the `jenv` utility, a convenience bash script [`buildScn.sh`](_bash/buildScn.sh) is created to build all Java demo programs.