-- Switch to the keyspace
USE workshop;

-- Create the Cassandra table based on the Avro schema
CREATE TABLE IF NOT EXISTS auto_products (
  product_id TEXT PRIMARY KEY,
  product_name TEXT,
  description TEXT,
  price DOUBLE,
  store_id TEXT
);

INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p1', 'Product One', 'This is product one', 100.50, 's1');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p2', 'Product Two', 'This is product two', 150.25, 's2');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p3', 'Product Three', 'This is product three', 75.99, 's3');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p4', 'Product Four', 'This is product four', 99.75, 's1');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p5', 'Product Five', 'This is product five', 120.00, 's2');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p6', 'Product Six', 'This is product six', 49.99, 's3');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p7', 'Product Seven', 'This is product seven', 89.50, 's1');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p8', 'Product Eight', 'This is product eight', 29.99, 's2');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p9', 'Product Nine', 'This is product nine', 199.99, 's3');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p10', 'Product Ten', 'This is product ten', 79.50, 's1');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p11', 'Product Eleven', 'This is product eleven', 59.75, 's2');
INSERT INTO auto_products (product_id, product_name, description, price, store_id) VALUES ('p12', 'Product Twelve', 'This is product twelve', 149.99, 's3');

