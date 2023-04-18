- [1. Overview](#1-overview)
  - [1.1. Deployment Properties File](#11-deployment-properties-file)
  - [1.2. Deploy the Scenario](#12-deploy-the-scenario)
- [2. Connect to the Pulsar Cluster](#2-connect-to-the-pulsar-cluster)
- [3. Pulsar Rest API](#3-pulsar-rest-api)
  - [3.1. Create the Tenant](#31-create-the-tenant)
  - [3.2. Create the Namespace](#32-create-the-namespace)
  - [3.3. Create the Topic](#33-create-the-topic)
  - [3.4. Update the Topic Schema](#34-update-the-topic-schema)
    - [3.4.1. Schema Config JSON String](#341-schema-config-json-string)
  - [3.5. Deploy the Function](#35-deploy-the-function)
    - [3.5.1. Function Configuration JSON File](#351-function-configuration-json-file)


---


# 1. Overview

The **`deploy.sh`** is needs to be executed as the first step. Each scenario has its own version of `deploy.sh` file and when running it, it will create a default set of Pulsar resources that are specific to that particular scenario. For example for the [`message-enrich`](./native-pulsar/message-enrichment/) scenario, the following default Pulsar resources will be created: 
* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/raw`
   * `msgenrich/testns/processed`

If you want to run the scenario against a different set of Pulsar tenant, namespace, and topics, you can also achieve so by specifying them in a deployment properties file.

## 1.1. Deployment Properties File

The deployment properties file has the following format

```
##
# (Mandatory) Must in format <tenant>/<namespace>
tenantNamespace=<teanant_name>/<namespace_name>
# (Mandatory) Comma separated core topic names without space
coreTopics=<topic_1>,<topic_2>,...

## 
# (Optional) Comma separated core function names without space
coreFunctions=<function_1>,<function_2>,...

##
# Cluster name (ONLY relevant for non-Astra Streaming Pulsar cluster)
nas.clusterName=<cluster_name>
```

Through this file,

1. You first can specify the Pulsar tenant, namespace, and topic names that you want to run this scenario against.
  
2. If the scenario requires deploying Pulsar functions, you need to put the list of the core function names (without "<tenant>/<namespace>" prefix) 
  
3. For non-Astra streaming based Pulsar cluster, you also need to specify the Pulsar cluster name so that the Pulsar tenant can be automatically created. For Astra streaming based Pulsar cluster, this is NOT needed.

## 1.2. Deploy the Scenario

Running the deployment script is easy, and it takes the following command line input parameters:
```
Usage: deploy.sh [-h]
                 -cc <client_conf_file>
                 [-na]
                 [-dp <deploy_properties_file>]
       -h  : Show usage info
       -cc : (Required) 'client.conf' file path.
       -na : (Optional) Non-Astra Streaming (Astra streaming is the default).
       -dp : (Optional) 'deploy.properties' file path (default to '<SCENARIO_HOMEDIR>/deploy.properties')
```

Among all the input parameters, the optional parameter `-dp` is used to specify a customer deployment properties file. Every scenario has a default one called, `deploy.properties`, under the scenario home directory. When running the `deploy.sh` script without the `-dp` option, it will read the Pulsar tenant, namespace, and topics information from the default deployment properties file.

If you want to run the scenario with non-default Pulsar resource names, you can either update the default deployment properties file or create a new one and run the `deploy.sh` script with the `-dp` option: 
```
deploy.sh -cc /tmp/client.conf -dp /path/to/customized_deployment_properties_file
```

# 2. Connect to the Pulsar Cluster

Both scripts need to be able to connect to a Pulsar cluster successfully before executing the actual tasks. The cluster connection information is provided via the `deploy.sh -cc` option. This option specifies the file path to a `client.conf` file that includes all the required information for connecting to a Pulsar cluster. Please see [Apache Pulsar doc](https://pulsar.apache.org/docs/2.10.x/reference-configuration/#client) for more information about this file.

Please **NOTE** that for Astra Streaming (AS), this requires creating an AS tenant in advance and downloading the corresponding `client.conf` from the UI. This is because AS is a managed service and as a client application, it is impossible to get the cluster admin token like in a self-managed Pulsar cluster. The AS token for a client application is always associated with a particular tenant.

# 3. Pulsar Rest API 

The `deploy.sh` script creates all Pulsar resources via the Pulsar rest API through the `curl` command. The benefit of doing so is you don't need to download or install any Pulsar admin client tools like *pulsar-admin* or *pulsar-shell*. Using these tools to create the corresponding Pulsar resources is easy and straightforward. Please refer to the [Pulsar Admin CLI doc](https://pulsar.apache.org/docs/2.11.x/reference-pulsar-admin/)

In the scenarios included in this repository, the REST APIs are used to create the following Pulsar resources. 

## 3.1. Create the Tenant

**NOTE**: The `deploy.sh` script will ONLY execute this step for non-AS based Pulsar deployment and this requires explicitly setting `deploy.sh -na` option because by default `deploy.sh` assumes dealing with an AS Pulsar cluster.

The rest API to create a Pulsar tenant is as below:
```
curl -sS -k -X PUT \
  --url 'https://<pulsar_websvc_url>/admin/v2/tenants/<tenant_name> \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <jwt_token>' \
  --data '{ \"allowedClusters\": [\"<cluster_name>\"] }'
```

## 3.2. Create the Namespace

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/namespaces/<tenant_name>/<namespace_name>' \
    --header 'Authorization: Bearer <jwt_token>'
```

## 3.3. Create the Topic

**NOTE** The `deploy.sh` script will always create a partitioned topic with 5 partitions, which should be good enough for the common demo scenarios.

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/persistent/${topicName}/partitions' \
    --header 'Authorization: Bearer <jwt_token>' \
    --header 'Content-Type: text/plain' \
    --data 5
```

## 3.4. Update the Topic Schema

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X POST \
    --url 'https://<pulsar_websvc_url>/admin/v2/schemas/<tenant>/<namespace>/<topic>/schema' \
    --header 'Authorization: Bearer <jwt_token>' \
    --header 'Content-Type: application/json' \
    --data '<schema_config_json_string>'
```

### 3.4.1. Schema Config JSON String

In the above command, the `--data` payload requires a JSON string that represents the schema. Please **NOTE** not all scenarios require updating topic schemas. An example of such a schema JSON string can be found in the scenario [message-enrichment-avro](./native-pulsar/message-enrichment-avro/_config/topic-schema.json). 

## 3.5. Deploy the Function

The rest API to deploy a Pulsar function is as below:
```
curl -sS -k -X POST \
    --url 'https://<pulsar_websvc_url>/admin/v3/functions/ymtest/default/<function_name>' \
    --header 'Authorization: Bearer <jwt_token> \
    --form 'data=@</path/to/function/jar/file>;type=application/octet-stream' \
    --form 'functionConfig=@</path/to/to/function/config/json/file>;type=application/json' \
    --write-out '%{http_code}'
```

### 3.5.1. Function Configuration JSON File

In order to deploy a Pulsar function this way, a function configuration JSON file is needed via the `--form 'functionConfig=...;type=application/json'` payload option. Please **NOTE** not all scenarios require deploying a Pulsar function. An example of such a function configuration JSON string can be found in the scenario [message-enrichment](./native-pulsar/message-enrichment/_config/add-metadata.json). 

Please **NOTE** that the `deploy.sh` file will NOT create this file for you. You need to create it in advance before running the script. The requirements for this file are:
1. The JSON file must use the function name as the file. 
   * In the above example, a funciton named `add-metadata` will be deployed.
2. The JSON file must be under `_config` sub-folder of the scenario home directory
```
config
└── add-metadata.json
```