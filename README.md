DataDrivenPolicy
================

Build Instruction

Before starting, make sure mongodb is running. You can check if its running by running the mongo command. If you get an error, run "sudo mongod"

1. cd to the build directory (cd changes directories)
2. Run the 'make' command
3. To query, pass your question as an argument to the GUI java program, and make sure "lib/\*" is in the classpath. i.e. while in the build directory
 * java -cp ".:../lib/\*" GUI "how is the FDA affected by roaming wild grizzly bears?"
4. Your data should be in the mongodb database now. To query it, do the following:
  * Open up the database by running the mongo command
  * Select our test database with "use test"
  * Each collection is a list of results from one of our queries. The name of the collection is the question asked. To get a list of collections, type "show collections"
  * To query a collection, use the following:
>     db.getCollection('How_can_the_FDA_effectively_regulate_personal_medicine_compared_to_“we”_medicine')
5. a


To run this application, you must have a copy of MongoDB running on localhost. Instruction on how to install and run MongoDB can be retrieved from http://docs.mongodb.org/manual/installation/
