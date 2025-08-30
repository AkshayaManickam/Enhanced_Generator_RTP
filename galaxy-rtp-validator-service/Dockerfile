FROM openjdk:21

#Set app home folder
ENV APP_HOME /app

#Create base app folder
RUN mkdir $APP_HOME

#Installing wget and packages
RUN echo -e "[main]\ntsflags=nodocs\n" > /etc/yum.conf \
 && microdnf install -y openssh-clients \
 && microdnf clean all \
 && rm -rf /var/cache/yum

WORKDIR $APP_HOME

ARG JAR_FILE

ADD target/${JAR_FILE} galaxy-rtp-validator-service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","galaxy-rtp-validator-service.jar"]
