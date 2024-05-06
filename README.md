# astra-transactions
This app can be used as a reference app to refactor a Cassandra based app that uses `logged batches` to manage transactions in DB layer to an app that manages transactions within the app layer. 
The app uses two tables, course (`batch_course`) and student (`batch_student`) to implement a transactional workflow where a student signs up for a course. 
When a student signup for a course, it always creates two records (one within each table) as part of a transaction i.e. course table will map course to student & student table will do vice-versa. These tables can then be used for two difference access paterns 
- View all the students that are taking a course (using table `batch_course`) and  
- View all the courses taken by a student (using table `batch_student`)  

Executing this sample app performs below sets of operations to demo performance of batch-inserts VS plain-inserts 
- Creates (if not exists) two tables, `batch_student` and `batch_course`. 
- Runs a set of `plain-inserts` on the above two tables and captures time taken to create and execute those CQL statements
- Runs a set of `batch-inserts` on the above two tables and captures time taken to create and execute those CQL statements
- Injects a failure (for one of the courses) and handles it as part of transaction for both modes (`batch` and `non-batch`)

Note: Actual replication latency will be usually a bit less then the one reported by the app, as there will be some delay between when the data appears in a region to when the app reads it. Also note that these are replication latencies between regions and not latency between the Client (your app) and Server (Astra DB).

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
	- Optionally you can also pass the `number-of-courses (default 10)`, `number-of-students (default 20)`, student-id to generate exception (default 15) to insert.
	Note: The above default params will create 400 statements (courses * students * 2 = 400). Do not create over 2K statements if this is not an enterprise DB that allows higher OPS/rate-limits.

## Sample Output
```
18:33:52.235 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_COURSE' has been created (if not exists) OR truncated (if exists).
18:33:52.666 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_STUDENT' has been created (if not exists) OR truncated (if exists).
18:33:53.078 INFO  com.bhatman.astra.TransactionApp: ======================= PERFORMING Transactional INSERTS with Batch: false =======================
18:33:53.087 INFO  com.bhatman.astra.TransactionApp: Took 9 milliseconds to create 30 statements
18:33:53.129 INFO  com.bhatman.astra.TransactionApp: Took 42 milliseconds to execute 30 statements with Batch: false
18:33:55.204 INFO  com.bhatman.astra.AppUtil: Closed connection!
18:33:57.492 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_COURSE' has been created (if not exists) OR truncated (if exists).
18:33:57.924 INFO  com.bhatman.astra.AppUtil: Table 'BATCH_STUDENT' has been created (if not exists) OR truncated (if exists).
18:33:58.248 INFO  com.bhatman.astra.TransactionApp: ======================= PERFORMING Transactional INSERTS with Batch: true =======================
18:33:58.252 INFO  com.bhatman.astra.TransactionApp: Took 4 milliseconds to create 15 statements
18:33:58.298 INFO  com.bhatman.astra.TransactionApp: Took 46 milliseconds to execute 15 statements with Batch: true
18:34:00.369 INFO  com.bhatman.astra.AppUtil: Closed connection!
```
