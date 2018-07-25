# MicroServices for DL4J(NLP) and TensorFlow(Image Classifier)

##Description
This repository contains implementation of NLP and Image Classifier Services as RESTful web services with Java(Jersey/JAX-RS)



## Requirements

1. Python 3 or above
2. pip installed
3. gradle 3.3 or above
4. Various modules of Python: *keras, matplotlib, theano, opencv-python, scikit-learn, pillow*
5. Apache tomcat 7 or above

    
    
    
## Steps to deploy this service:
1. Git clone the repository in linux env.
2. Run 'gradle --debug clean build' from the <project directory> where build.gradle is placed.
3. Copy  '<project directory>/build/libs/MicroServices_DL4J_TF.war' to 'CATALINA_HOME/webapps'.   'CATALINA_HOME' is the path of Apache tomcat setup.
4. Change the default port number in <CATALINA_HOME>/conf/server.xml, from *8080* to *5555*
5. Add an 'admin' user to access manager console. *Edit <CATALINA_HOME>/conf/tomcat-users.xml*
6. Start the Apache tomcat server with command '<CATALINA_HOME>/bin/startup.sh'. Access URL: http://<hostname>:5555/MicroServices_DL4J_TF to validate if war file deployed correctly, else check the log.




####Other Infos:
1. Logs of the server can be accessed in <CATALINA_HOME>/logs/catalina.out
2. All dependency libraries are downloaded by gradle in path: *~/.gradle/caches/modules-2/files-2.1*. For example */home/<USER_NAME>/.gradle/caches/modules-2/files-2.1*. So, if you want to delete cache, delete cache directory
3. To fix lock file error, delete all the *.lock file with following command: *find ~/.gradle -type f -name "*.lock" | while read f; do rm $f; done*
4. While setting proxy in terminal and browswer, make sure to set port no. as *80* and not *8080*
