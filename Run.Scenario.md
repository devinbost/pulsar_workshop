- [1. Overview](#1-overview)
- [2. Connect to the Pulsar Cluster](#2-connect-to-the-pulsar-cluster)
- [3. Raw Input Data: IoT Sensor Reading Data](#3-raw-input-data-iot-sensor-reading-data)


---


# 1. Overview

No matter which messaging processing API or protocol we're working with, message publishing/producing/sending and message subscribing/consuming/receiving are two common fundamental activities to take. Because of this, each scenario in this repository provides 2 bash scripts: `runProducer.sh` and `runConsumer.sh`.

Across all scenarios, these 2 scripts share some common command line input parameters such as:
* `-n` for how many messages to process
* `-t` for which Pulsar topic to interact with
* `-cc` for the connection information to the Pulsar cluster (see below)

However, for different scenarios they may take some extra command line input parameters that are unique to each scenario. In order to find out the exact details for each scenario, please run the `-h` option to find out:

```
runProducer.sh -h
```

or

```
runConsumer.sh -h
```

# 2. Connect to the Pulsar Cluster

Both scripts need to be able to connect to a Pulsar cluster successfully before executing the actual tasks. The cluster connection information is provided via the `runProducer.sh -cc` or `runConsumer.sh -cc` option. This option specifies the file path to a `client.conf` file that includes all the required information for connecting to a Pulsar cluster. Please see [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client) for more information about this file.

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 3. Raw Input Data: IoT Sensor Reading Data

For many of the scenarios in this repository, instead of letting random messages being published/produced/sent to a topic, the "producer" client application reads data from an actual **IoT sensor reading data** as the raw input data. These data are from a CSV file that is available from [sensor_telemetry.csv](./scenarios/_raw_data_src//sensor_telemetry.csv). Each line of the CSV file represents a particular IoT device reading of the following types at a particular time.
* Carbon monoxide
* Humidity (%)
* Light detection
* Liquefied petroleum gas (LPG)
* Motion detection
* Smoke
* Temperature

For a more detailed description of this data source, please check from [here](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k).