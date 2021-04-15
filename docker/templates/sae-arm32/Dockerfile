FROM arm32v7/openjdk:11

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV PATH="/usr/bin:${PATH}"
ENV JAVA_OPTS="-Duser.language=de -Duser.country=DE"
ENV SAE_INSTALL_DIR=/opt/sae
ENV SAE_HOME=/opt/sae/data
ENV SAE_VERSION=@project.version@

EXPOSE 8080

RUN echo "Europe/Berlin" > /etc/timezone
RUN cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime

RUN mkdir -p $SAE_INSTALL_DIR && \
    curl -o $SAE_INSTALL_DIR/logback-spring.xml -L -k https://github.com/camueller/SmartApplianceEnabler/raw/master/run/logback-spring.xml && \
    curl -o $SAE_INSTALL_DIR/SmartApplianceEnabler-$SAE_VERSION.war -L -k https://github.com/camueller/SmartApplianceEnabler/releases/download/$SAE_VERSION/SmartApplianceEnabler-$SAE_VERSION.war

COPY libwiringPi.so.2.50 /lib/arm-linux-gnueabihf/libwiringPi.so
COPY libwiringPiDev.so.2.50 /lib/arm-linux-gnueabihf/libwiringPiDev.so
COPY gpio /usr/bin

CMD java $JAVA_OPTS -Djava.awt.headless=true -Xmx256m -Dsae.home=$SAE_HOME -Dlogging.config=$SAE_INSTALL_DIR/logback-spring.xml -jar $SAE_INSTALL_DIR/SmartApplianceEnabler-$SAE_VERSION.war
