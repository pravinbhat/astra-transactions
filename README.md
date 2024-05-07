# astra-transactions
This app can be used as a reference app to refactor a Cassandra based app that uses `logged batches` to manage transactions in DB layer to an app that manages transactions within the app layer. 

Note: When you perform a batch-insert, the idea is to let the DB manage the transaction. Although this may work fine, it has its downside due to Cassandra not being an ACID DB as well as batch being usually considerred as an anti-pattern with Cassandra. This app provides an alternative, where the transaction is instead managed in the app layer. The tradeoff is that while it puts additional responsibilities on the app layer, it will scale better & is a better bit for use-cases where such transactions are critical & have a large count of operations.  

The app uses two tables, course (`batch_course`) and student (`batch_student`) to implement a transactional workflow where a student signs up for a course. 
When a student signup for a course, it always creates two records (one within each table) as part of a transaction i.e. course table will map course to student & student table will do vice-versa. These tables can then be used for two difference access paterns 
- View all the students that are taking a course (using table `batch_course`) and  
- View all the courses taken by a student (using table `batch_student`)  

Executing this sample app performs below sets of operations to demo performance of batch-inserts VS plain-inserts 
- Creates (if not exists) two tables, `batch_student` and `batch_course`. 
- Runs a set of `plain-inserts` on the above two tables and captures time taken to create and execute those CQL statements
- Runs a set of `batch-inserts` on the above two tables and captures time taken to create and execute those CQL statements
- Injects a failure (for one of the courses) and handles it as part of transaction


## Prerequisite
- **Java11** (minimum) or higher
- **Maven 3.9.x** (minimum) or higher

## Building Jar 
1. Clone this repo
2. Move to the repo folder `cd astra-transactions`
3. Run the build `mvn clean package -Passembly`
4. The fat jar (`target/astra-transactions-*-jar-with-dependencies.jar`) file should now be present in the `target` folder

## Running the App
- Provide the secure-connect-bundle (SCB) for the Astra DB
- Provide the Astra Client_ID 
- Provide the Astra SECRET 
- Run command `java -jar target/astra-transactions-*-jar-with-dependencies.jar "path-to-scb-region" "client-id" "client-secret"`
	- Optionally you can also pass the `number-of-courses (default 10)`, `number-of-students (default 20)`, student-id to generate exception (default 15).
	- The above default params will create 400 statements (courses * students * 2 = 400). 

## Sample Output
```
12:20:36.968 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_COURSE' has been created (if not exists) OR truncated (if exists).
12:20:45.474 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_STUDENT' has been created (if not exists) OR truncated (if exists).
12:20:45.837 INFO  com.bhatman.astra.TransactionApp: ======== PERFORMING transactional INSERTS for 100 Courses and 1000 Students with Batch: true ========
12:20:46.108 INFO  com.bhatman.astra.TransactionApp: Took 271 milliseconds to create 100000 statements
12:21:07.852 INFO  com.bhatman.astra.TransactionApp: Took 21743 milliseconds to execute 100000 statements with Batch: true
12:21:09.936 INFO  com.bhatman.astra.AppUtil: Closed connection!
12:21:12.419 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_COURSE' has been created (if not exists) OR truncated (if exists).
12:21:13.356 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_STUDENT' has been created (if not exists) OR truncated (if exists).
12:21:13.620 INFO  com.bhatman.astra.TransactionApp: ======== PERFORMING transactional INSERTS for 100 Courses and 1000 Students with Batch: false ========
12:21:13.762 INFO  com.bhatman.astra.TransactionApp: Took 142 milliseconds to create 200000 statements
12:21:30.523 INFO  com.bhatman.astra.TransactionApp: Took 16760 milliseconds to execute 200000 statements with Batch: false
12:21:32.590 INFO  com.bhatman.astra.AppUtil: Closed connection!
```
