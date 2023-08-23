

To create the Avro schema from the initial C* table definition, that can be done with GPT-4:

`"Convert this Cassandra table definition into an Avro schema:
CREATE TABLE device.device_ts ( tag_id text, data_quality int, event_time timestamp, event_value double)
"`

GPT-4 can be used similarly to generate JSON examples with that schema.

To build POJO from Avro schema:

`mvn clean package`

To create the downstream tables:

`create table device.device_ts_new ( tag_id text, yyyymm int, data_quality int, event_time timestamp, event_value double, PRIMARY KEY ((tag_id, yyyymm, data_quality), event_time)) WITH CLUSTERING ORDER BY (event_time DESC) and default_time_to_live = 0 AND gc_grace_seconds = 864000 and max_index_interval = 2048;`

`create table device.current_value ( tag_id text, data_quality int, event_time timestamp, event_value double, PRIMARY KEY ((tag_id, data_quality), event_time)) WITH CLUSTERING ORDER BY (event_time DESC) and default_time_to_live = 0 AND gc_grace_seconds = 864000 and max_index_interval = 2048;`


Create config.properties file in the resources directory for any function that requires credentials. 
Be careful to not commit this file to your repo!


For OpenAI examples:
`CREATE TABLE IF NOT EXISTS openai.order_with_embedding (order_id int, customer_id int, customer_first_name text, customer_last_name text, customer_email text, customer_phone text, customer_address text, product_id int, product_name text, product_description text, product_price double, order_quantity int, order_date text, total_amount double, shipping_address text, embedding VECTOR<float,1536>, PRIMARY KEY ((order_id), customer_id));`
`CREATE INDEX IF NOT EXISTS on openai.order_with_embedding(customer_id);`
`CREATE CUSTOM INDEX IF NOT EXISTS product_ann_index ON openai.order_with_embedding(embedding) USING 'StorageAttachedIndex'`


CREATE TABLE IF NOT EXISTS openai.order_with_embedding (order_id int, customer_id int,  , PRIMARY KEY ((order_id), customer_id));

