# emarket
 simple part from an ecommerce website
 
 ## Used Technologies
 - Spring Boot
 - JPA
 - H2 Database (in memory database for simplicity)
 - Swagger
 - RabbitMQ
 - Docker
 - Docker-Compose

## To be implemented later
- Caching layer (Redis)
- API Gateway
- FTP server to save the csv files
 
 ## Services
 Each service has its own DB and they communicate with each other through rabbitmq
 
 - Product Service
   manages the products and its stock per country. It has its own products DB and it exposes only one API to retreive a product by SKU
   it works on port 8080
 - Order
   manages the orders which are used later to consume product's stock. It has its own Orders DB and it exposes two APIs
   one API to consume a specific product stock from a specific country, and another API to upload csv file and use it to create bulk of orders and process them.
   it works on port 8081
   
   ## APIs
   - Get product by SKU
     - URL: http://[server]:8080/products/{sku}
     - HTTP Method: GET
     - Description: return a single product from DB by its SKU, and returns error 404 if the product not exist
     - Notes: no cache implemented yet
   - Create Order
     - URL: http://[server]:8081/orders
     - HTTP Method: POST
     - Content-Type: application/json
     - Body: `{
               "sku": "abc123",
               "name": "pen",
               "country": "eg",
               "quantity": 3
              }`
     - Description: 
       1. order request received and saved to DB with status set to pending
       2. a message published to an orderPlaced queue that contains the new order
       3. a response sent to customer withe the order location specified in the response header
       4. the Product service consumes the order message from the orderPlaced queue
       5. the Product service makes sure the product exist and the required stock is available in the specified country
       6. if the order is valid then the Product service decreases the product stock from the specified country
       7. the Product service update the order status according to the result from step 5
       8. the Product service publish a new message to an orderProcessed queue that contains the updated order
       9. the Order service consumes the processed order from the orderProcessed queue
       10. the Order service updates the DB with the updated order status
     - Notes: tried to implement SAGA pattern to ensure the order is processed and the stock decreased only if the stock is available in the specified country, and to add any business rules later by any other service like customer credit for example.
   - Create bulk
       - URL: http://[server]:8081/orders/bulk
       - HTTP Method: POST
       - Content-Type: multipart/form-data
       - Body: parameter name=file, parameter value=csv file
       - Description: 
         1. file received by the Order service and saved in local disk
         2. a message published to a csvProcessing queue that contains the saved file's new name
         3. a response sent to the customer with 202 accepted status
         4. the Order service consumes the file name from the csvProcessing queue
         5. the Order service reads the file content and starts to process its records one by one by creating a new order for each record
         6. the Order service saves the records content in the orders DB table
         7. each new order get processed same as the 'Create Order' scenario except that we set the Order source as bulk
         8. in the Product service if the product is not exist we create it and add an initial stock for the record's country
         9. the Product service process the order and update its status same as the 'Create Order' scenario
       - Notes: for simplicity I saved the csv file on the local disk, but in real world scenario it should be sent to FTP server. also in read world scenario a record should be saved in a DB (can be nosql document based DB) so we can keep track of the file status later on and recover from where we stop in case of any issues
    

