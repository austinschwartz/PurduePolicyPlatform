DataDrivenPolicy
================

Build Instruction

Before starting, make sure mongodb is running. You can check if its running by running the mongo command. If you get an error, run "sudo mongod"

1. cd to the build directory (cd changes directories)
2. Run the 'make' command
3. To query, pass your question as an argument to the GUI java program, and make sure "lib/\*" is in the classpath. i.e. while in the build directory
 * java -cp ".:../lib/\*" GUI "how is the FDA affected by roaming wild grizzly bears?"
4. Your data should be in the mongodb database now. To query it, do the following:
  a. asd
  b. de
5. a


To run this application, you must have a copy of MongoDB running on localhost. Instruction on how to install and run MongoDB can be retrieved from http://docs.mongodb.org/manual/installation/
