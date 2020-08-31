FROM openjdk:11

WORKDIR /root

ADD listenability-tools-*-all.jar /root
ADD service.conf /root

ENTRYPOINT java -cp listenability-tools-*-all.jar de.aitools.commons.uima.Serve --configuration service.conf

