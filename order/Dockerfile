FROM openjdk:8-jdk-alpine
MAINTAINER Adel
COPY target/order-0.0.1-SNAPSHOT.jar order-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/order-0.0.1-SNAPSHOT.jar","-web -webAllowOthers -tcp -tcpAllowOthers -browser"]