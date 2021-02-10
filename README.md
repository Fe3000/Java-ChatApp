# Java-ChatApp
Trying to make a chat application using jBoss 5.0.0 and JMS API.

Currently it is planned to write and read the messages sent in the command prompt, but will later have a window and layout using JavaFX.

## Dependecy
This project needs jBoss extracted to a directory (e.g. /server).

## Testing
### Testing with the JAR files
In Eclipse, right-click on the Java-ChatApp project and Export, Java, Runnable JAR file to a dir (e.g. /test)
In the jBoss dir, (/server) open cmd and write run.  This will start the server. 
Then to open a client go to the test dir (/test) where the JAR is and open another cmd run: java -jar chat.jar <username>
  
### Testing in Eclipse
In the jBoss dir, (/server) open cmd and write run.  This will start the server. 
Press Run in Eclipse. The client should now be connected and ready.
