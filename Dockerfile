#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package  -Ddocker.skip.build=true -Ddocker.skip=true -DSkipTests

#
# Package stage
#
FROM fabric8/s2i-java:2.3
USER root

COPY sirc.cer $JAVA_HOME/jre/lib/security/
COPY a392f9bbb8c2f899.crt $JAVA_HOME/jre/lib/security/
##RUN \
##    cd $JAVA_HOME/jre/lib/security \
##    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias springboot -file sirc.cer \
##    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias sirc -file a392f9bbb8c2f899.crt
COPY --from=build /home/app/target/*.jar /usr/local/lib/demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djavax.net.debug=all","-jar","/usr/local/lib/demo.jar"]
