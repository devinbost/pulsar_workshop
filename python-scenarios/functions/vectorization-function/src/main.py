from pulsar import Function
import pulsar
from pulsar.schema import AvroSchema
from fastavro import parse_schema, schemaless_reader
from fastavro import writer, reader
import io
from io import BytesIO
import openai
from tenacity import retry, wait_random_exponential, stop_after_attempt

class OrderProcessingFunction(Function):
    def __init__(self):
        self.priorSchema = parse_schema({
          "type": "record",
          "name": "Order",
          "fields": [
            {"name": "OrderID", "type": "int"},
            {"name": "CustomerID", "type": "int"},
            {"name": "CustomerFirstName", "type": "string"},
            {"name": "CustomerLastName", "type": "string"},
            {"name": "CustomerEmail", "type": "string"},
            {"name": "CustomerPhone", "type": "string"},
            {"name": "CustomerAddress", "type": "string"},
            {"name": "ProductID", "type": "int"},
            {"name": "ProductName", "type": "string"},
            {"name": "ProductDescription", "type": "string"},
            {"name": "ProductPrice", "type": "double"},
            {"name": "OrderQuantity", "type": "int"},
            {"name": "OrderDate", "type": "string"},
            {"name": "TotalAmount", "type": "double"},
            {"name": "ShippingAddress", "type": "string"}
          ]
        })
        self.newSchema = parse_schema({
          "type": "record",
          "name": "Order",
          "fields": [
            {"name": "OrderID", "type": "int"},
            {"name": "CustomerID", "type": "int"},
            {"name": "CustomerFirstName", "type": "string"},
            {"name": "CustomerLastName", "type": "string"},
            {"name": "CustomerEmail", "type": "string"},
            {"name": "CustomerPhone", "type": "string"},
            {"name": "CustomerAddress", "type": "string"},
            {"name": "ProductID", "type": "int"},
            {"name": "ProductName", "type": "string"},
            {"name": "ProductDescription", "type": "string"},
            {"name": "ProductPrice", "type": "double"},
            {"name": "OrderQuantity", "type": "int"},
            {"name": "OrderDate", "type": "string"},
            {"name": "TotalAmount", "type": "double"},
            {"name": "ShippingAddress", "type": "string"},
            {
                "name": "Embedding",
                "type": [
                "null",
                {
                    "type": "array",
                    "items": "float" # Note: Some embedding APIs use doubles instead of floats
                }
                ]
            }
          ]
        })

    @retry(wait=wait_random_exponential(min=1, max=20), stop=stop_after_attempt(6))
    def get_embedding(text: str, model="text-embedding-ada-002") -> list[float]:
        return openai.Embedding.create(input=[text], model=model)["data"][0]["embedding"]
        
    def process(self, input, context):
        message = schemaless_reader(io.BytesIO(input), self.priorSchema)
        message['Embedding'] = self.get_embedding(message['ProductName'] + " - " + message['ProductDescription']) 

        return message

    def process_order(self, order, context):
        # TODO: Replace with actual processing logic
        print(f"Processing order: {order['OrderID']}")

class Order:
    def __init__(self, OrderID, CustomerID, CustomerFirstName, CustomerLastName, CustomerEmail, CustomerPhone, CustomerAddress, ProductID, ProductName, ProductDescription, ProductPrice, OrderQuantity, OrderDate, TotalAmount, ShippingAddress):
        self.OrderID = OrderID
        self.CustomerID = CustomerID
        self.CustomerFirstName = CustomerFirstName
        self.CustomerLastName = CustomerLastName
        self.CustomerEmail = CustomerEmail
        self.CustomerPhone = CustomerPhone
        self.CustomerAddress = CustomerAddress
        self.ProductID = ProductID
        self.ProductName = ProductName
        self.ProductDescription = ProductDescription
        self.ProductPrice = ProductPrice
        self.OrderQuantity = OrderQuantity
        self.OrderDate = OrderDate
        self.TotalAmount = TotalAmount
        self.ShippingAddress = ShippingAddress

class OrderWithEmbedding:
    def __init__(self, order, embedding):
        self.OrderID = order.OrderID
        self.CustomerID = order.CustomerID
        self.CustomerFirstName = order.CustomerFirstName
        self.CustomerLastName = order.CustomerLastName
        self.CustomerEmail = order.CustomerEmail
        self.CustomerPhone = order.CustomerPhone
        self.CustomerAddress = order.CustomerAddress
        self.ProductID = order.ProductID
        self.ProductName = order.ProductName
        self.ProductDescription = order.ProductDescription
        self.ProductPrice = order.ProductPrice
        self.OrderQuantity = order.OrderQuantity
        self.OrderDate = order.OrderDate
        self.TotalAmount = order.TotalAmount
        self.ShippingAddress = order.ShippingAddress
        self.Embedding = embedding