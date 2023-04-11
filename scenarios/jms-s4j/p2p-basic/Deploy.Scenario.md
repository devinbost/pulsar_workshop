- [1. Overview](#1-overview)
  - [1.1. Deployment Properties File](#11-deployment-properties-file)
- [2. Deploy the Scenario](#2-deploy-the-scenario)
  - [2.1. Pulsar Rest API](#21-pulsar-rest-api)
  - [2.2. Create the Tenant](#22-create-the-tenant)
  - [2.3. Create the Namespace](#23-create-the-namespace)
  - [2.4. Create the Topic](#24-create-the-topic)


# 1. Overview

As mentioned in the main doc, by default this scenario is executed against the following Pulsar tenant, namespace, and topics.
* **tenant**: `msgenrich`
* **namespace**: `testns`
* **topics**:
   * `msgenrich/testns/s4j_p2p`

If you want to run the scenario against a different set of Pulsar tenant, namespace, and topics, you can also achieve so by specifying them in a deployment properties file.

## 1.1. Deployment Properties File

The deployment properties file has the following format

```
##
# (Mandatory) Must in format <tenant>/<namespace>
tenantNamespace=msgenrich/testns
# (Mandatory) Comma separated core topic names without space
coreTopics=s4j_p2p

##
# Cluster name (ONLY relevant for non-Astra Streaming Pulsar cluster)
nas.clusterName=mypulsar
```

Through this file,

1. You first can specify the Pulsar tenant, namespace, and topic names that you want to run this scenario against.
  
2. If the scenario requires deploying Pulsar functions, you need to put the list of the core function names (without "<tenant>/<namespace>" prefix) 
  
3. For non-Astra streaming based Pulsar cluster, you also need to specify the Pulsar cluster name so that the Pulsar tenant can be automatically created. For Astra streaming based Pulsar cluster, this is NOT needed.

# 2. Deploy the Scenario

This scenario has a default deployment properties file, `deploy.properties`, under the scenario home directory. When running the deployment script, `deploy.sh` without the `-dp` option, the script will read the Pulsar tenant, namespace, and topics information from the default deployment properties file.

You can also specify a customized deployment properties file using the following format
```
deploy.sh -cc /tmp/client.conf -dp /path/to/customized_deployment_properties_file
```

## 2.1. Pulsar Rest API 

The `deploy.sh` script creates all Pulsar resources via the Pulsar rest API through the `curl` command. The benefit of doing so is you don't need to download or install any Pulsar admin client tools like *pulsar-admin* or *pulsar-shell*. Using these tools to create the corresponding Pulsar resources is easy and straightforward. Please refer to the [Pulsar Admin CLI doc](https://pulsar.apache.org/docs/2.11.x/reference-pulsar-admin/)

## 2.2. Create the Tenant

**NOTE**: The `deploy.sh` script will ONLY execute this step for non-AS based Pulsar deployment and this requires explicitly setting `deploy.sh -na` option because by default `deploy.sh` assumes dealing with an AS Pulsar cluster.

The rest API to create a Pulsar tenant is as below:
```
curl -sS -k -X PUT \
  --url 'https://<pulsar_websvc_url>/admin/v2/tenants/<tenant_name> \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <jwt_token>' \
  --data '{ \"allowedClusters\": [\"<cluster_name>\"] }'
```

## 2.3. Create the Namespace

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/namespaces/<tenant_name>/<namespace_name>' \
    --header 'Authorization: Bearer <jwt_token>'
```

## 2.4. Create the Topic

**NOTE** The `deploy.sh` script will always create a partitioned topic with 5 partitions, which should be good enough for the common demo scenarios.

The rest API to create a Pulsar namespace is as below:
```
curl -sS -k -X PUT \
    --url 'https://<pulsar_websvc_url>/admin/v2/persistent/${topicName}/partitions' \
    --header 'Authorization: Bearer <jwt_token>' \
    --header 'Content-Type: text/plain' \
    --data 5
```