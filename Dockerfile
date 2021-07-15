#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:8-jdk-alpine
USER root
COPY US_export_policy.jar  $JAVA_HOME/jre/lib/security/
COPY local_policy.jar  $JAVA_HOME/jre/lib/security/
COPY java.security  $JAVA_HOME/jre/lib/security/
COPY sirc.cer $JAVA_HOME/jre/lib/security
RUN \
    cd $JAVA_HOME/jre/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias springboot -file sirc.cer
COPY --from=build /home/app/target/*.jar /usr/local/lib/demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djavax.net.debug=all","-jar","/usr/local/lib/demo.jar"]
