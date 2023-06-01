Steps:
1. Create database "cdcdemo4" with keyspace "ddemo" in us-central-1 and download token details
2. Create streaming tenant "cdctest04" in uscentral1
3. Create table "ddemo.delivery_details" from script: CREATE TABLE ddemo.delivery_details ("CustomerId" int, "CategoryId" int, "CustomerCity" text, "CustomerCountry" text, "CustomerFname" text, "CustomerLname" text, "Delivery_Status" text, "Late_delivery_risk" boolean, "OrderId" int PRIMARY KEY, "OrderStatus" text ) WITH additional_write_policy = '99PERCENTILE' AND bloom_filter_fp_chance = 0.01 AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'} AND comment = '' AND compaction = {'class': 'org.apache.cassandra.db.compaction.UnifiedCompactionStrategy'} AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'} AND crc_check_chance = 1.0 AND default_time_to_live = 0 AND gc_grace_seconds = 864000 AND max_index_interval = 2048 AND memtable_flush_period_in_ms = 0 AND min_index_interval = 128 AND read_repair = 'BLOCKING' AND speculative_retry = '99PERCENTILE';
4. Create table "ddemo.changes_by_order_id" from script: CREATE TABLE ddemo.changes_by_order_id (old_customer_id INT, old_late_delivery_risk BOOLEAN, old_order_status TEXT, old_delivery_status TEXT, old_customer_lname TEXT, old_customer_city TEXT, old_customer_country TEXT, old_customer_fname TEXT, old_order_id INT, old_category_id INT, old_updated_time BIGINT, new_customer_id INT, new_late_delivery_risk BOOLEAN, new_order_status TEXT, new_delivery_status TEXT, new_customer_lname TEXT, new_customer_city TEXT, new_customer_country TEXT, new_customer_fname TEXT, new_order_id INT, new_category_id INT, new_updated_time BIGINT, primary key ( ( new_order_id ) )  );
5. Enable CDC for tables "delivery_details" and "changes_by_order_id"
6. Generate a Database Admin token. Then, update secret, clientId, and tenant in config.properties for function.
Then, update secure bundle.
7. Run `mvn clean package`.
8. Go to streaming page and download client.conf
9. Copy client.conf into local Pulsar directory (after downloading Pulsar binaries)
10. Open console to Pulsar directory
11. In UI, create:
 namespaces;
    processing
    delivery-status
 persistent topics:
In $TENANT/processing:
  delivery-changes
  routed-changes
In $TENANT/delivery-status:
  pending-payment
  out-for-delivery
  complete
12. Update tenant and input topic in this command and run it:
  export CDC1=persistent://cdctest04/astracdc/data-367fdb1f-2fce-4284-ab38-6e8f1e986dbc-ddemo.delivery_details
  bin/pulsar-admin functions create --name changewriter --jar /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/astra-demo-0.0.1-jar-with-dependencies.jar --classname com.datastax.demo.dehlivery.function.AstraChangeWriter -o persistent://cdctest04/processing/delivery-changes --tenant cdctest04 --namespace processing -i $CDC1
13. Update tenant and input topic in this command and run it:
  export CDC2=persistent://cdctest04/astracdc/data-367fdb1f-2fce-4284-ab38-6e8f1e986dbc-ddemo.changes_by_order_id
  bin/pulsar-admin functions create --name changerouter --jar /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/astra-demo-0.0.1-jar-with-dependencies.jar --classname com.datastax.demo.dehlivery.function.AstraRouter -i $CDC2 -o persistent://$TENANT/processing/routed-changes --tenant $TENANT --namespace processing
14. Insert into delivery_details table to propagate schema:
  INSERT INTO ddemo.delivery_details ("CustomerId", "CategoryId", "CustomerCity", "CustomerCountry", "CustomerFname", "CustomerLname", "Delivery_Status", "Late_delivery_risk", "OrderId", "OrderStatus") VALUES ( 2432 , 95 , 'Delhi' , 'India' , 'Bob' , 'Smith' , 'Late delivery' , True , 11111 , 'PENDING_PAYMENT');
15. Create Organization Admin role token: https://astra.datastax.com/org/4c4a3356-5105-4354-b8ae-21afc799859e/settings/tokens
16. Create C* sink connector. # (If schema doesn't automatically appear, then troubleshoot.)
17. Create records:
INSERT INTO ddemo.delivery_details ("CustomerId", "CategoryId", "CustomerCity", "CustomerCountry", "CustomerFname", "CustomerLname", "Delivery_Status", "Late_delivery_risk", "OrderId", "OrderStatus") VALUES ( 2432 , 95 , 'Caguas' , 'Puerto Rico' , 'Jimmy' , 'Dean' , 'Late delivery' , True , 25454 , 'PENDING_PAYMENT');
INSERT INTO ddemo.delivery_details ("CustomerId", "CategoryId", "CustomerCity", "CustomerCountry", "CustomerFname", "CustomerLname", "Delivery_Status", "Late_delivery_risk", "OrderId", "OrderStatus") VALUES ( 2332 , 95 , 'Caguas' , 'Puerto Rico' , 'Jimmy' , 'Dean' , 'Late delivery' , True , 15454 , 'OUT_FOR_DELIVERY');
INSERT INTO ddemo.delivery_details ("CustomerId", "CategoryId", "CustomerCity", "CustomerCountry", "CustomerFname", "CustomerLname", "Delivery_Status", "Late_delivery_risk", "OrderId", "OrderStatus", "test") VALUES ( 2432 , 95 , 'Caguas' , 'Puerto Rico' , 'Jimmy' , 'Dean' , 'Late delivery' , True , 25454 , 'OUT_FOR_DELIVERY', 'example');
18. Subscribe pulsar-client:
bin/pulsar-client consume persistent://cdctest04/delivery-status/pending-payment -s test-dbost -n 0
bin/pulsar-client consume persistent://cdctest04/delivery-status/out-for-delivery -s test-dbost -n 0
bin/pulsar-client consume persistent://cdctest04/delivery-status/complete -s test-dbost -n 0

19.
INSERT INTO ddemo.delivery_details ("CustomerId", "CategoryId", "CustomerCity", "CustomerCountry", "CustomerFname", "CustomerLname", "Delivery_Status", "Late_delivery_risk", "OrderId", "OrderStatus") VALUES ( 1233 , 95 , 'Caguas' , 'Puerto Rico' , 'Jimmy' , 'Dean' , 'Late delivery' , True , 25454 , 'COMPLETE');

20. Observe that message flows through to clients

21. Zip lambda function:
cd /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/src/main/Python
zip /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/lambda-deployment-package.zip lambda.py
# If including dependencies, follow the instructions here: https://docs.aws.amazon.com/lambda/latest/dg/python-package.html
22. Create IAM role:

{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "kinesis:DescribeStream",
                "kinesis:DescribeStreamSummary",
                "kinesis:GetRecords",
                "kinesis:GetShardIterator",
                "kinesis:ListShards",
                "kinesis:ListStreams",
                "kinesis:SubscribeToShard",
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "s3-object-lambda:WriteGetObjectResponse"
            ],
            "Resource": "*"
        }
    ]
}
23. aws lambda create-function --function-name dehlivery-py-demo \
        --zip-file fileb:///Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/lambda-deployment-package.zip  \
        --role arn:aws:iam::825808906777:role/lambda-kinesis-role-dbost \
        --handler lambda.lambda_handler \
        --runtime python3.9
24. Create Kinesis stream:
aws kinesis create-stream --stream-name dehlivery-lambda-stream-dbost --shard-count 1
25. Get ARN via:
aws kinesis describe-stream --stream-name dehlivery-lambda-stream-dbost

26. Use ARN from above output for this command:
aws lambda create-event-source-mapping --function-name dehlivery-py-demo \
--event-source  arn:aws:kinesis:us-east-1:825808906777:stream/dehlivery-lambda-stream-dbost \
--batch-size 1 --starting-position LATEST

27. Verify the status is enabled:
aws lambda list-event-source-mappings --function-name dehlivery-py-demo --event-source arn:aws:kinesis:us-east-1:825808906777:stream/dehlivery-lambda-stream-dbost


Configure Elastic mapping.

Create data view with index in Analytics -> Discover
Provide username and password. Skip token and API key fields.
Provide URL from instance.
Turn Ignore Record key to true
Set strip nulls to false
Enable Schema
Enable Copy Key Fields

# This creates a timeseries of changes by default. However, if you wanted to update the record in Elastic, you could write an update instead.

# bin/pulsar-admin functions update --name changewriter --jar /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/astra-demo-0.0.1-jar-with-dependencies.jar --classname com.datastax.demo.dehlivery.function.AstraChangeWriter -o persistent://cdctest04/processing/delivery-changes --tenant cdctest04 --namespace processing

# ./pulsar-admin functions update --name changerouter --jar /Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/astra-demo-0.0.1-jar-with-dependencies.jar --classname com.datastax.demo.dehlivery.function.AstraRouter -o persistent://cdctest01/processing/routed-changes --tenant cdctest01 --namespace processing

# aws lambda update-function-code --function-name dehlivery-py-demo --zip-file fileb:///Users/devin.bost/proj/demos/dehlivery_demo_astra/java/astra-demo/target/lambda-deployment-package.zip
# INSERT INTO ddemo.changes_by_order_id (old_customer_id , old_late_delivery_risk , old_order_status , old_delivery_status , old_customer_lname , old_customer_city , old_customer_country , old_customer_fname , old_order_id , old_category_id , old_updated_time, new_customer_id , new_late_delivery_risk , new_order_status , new_delivery_status , new_customer_lname , new_customer_city , new_customer_country , new_customer_fname , new_order_id , new_category_id , new_updated_time) VALUES (121, true, 'PENDING_PAYMENT', 'N/A', 'Dean', 'Caguas', 'Puerto Rico', 'Jimmy', 32423, 1221, 823748293, 121, true, 'OUT_FOR_DELIVERY', 'Late delivery', 'Dean', 'Caguas', 'Puerto Rico', 'Jimmy', 32423, 1221, 823749293);

Create updated diagram.
Prepare all the commands
Streamline
Clean up what is open
INSERT
UPDATE ddemo.delivery_details set "OrderStatus"='PENDING_PAYMENT' where "CustomerId"=2432;
DELETE


For kinesis sink:
  Endpoint port: (leave blank)
  skip cert validation: false
  format: FULL_MESSAGE_IN_JSON
  Json include non-nulls: true
  others are defaults
  AWS endpoint: kinesis.us-east-1.amazonaws.com
  aws region: us-east-1
  stream name: dehlivery-lambda-stream-dbost
  credentials: {"accessKey":"AKIA4ARQL5IMU56QSYKI","secretKey":"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"}
https://kinesis.us-east-1.amazonaws.com
us-east-1
dehlivery-lambda-stream-dbost


DELETE FROM ddemo.delivery_details WHERE "CustomerId" = 1233;