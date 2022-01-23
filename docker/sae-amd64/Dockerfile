FROM amd64/openjdk:11

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
    curl -o $SAE_INSTALL_DIR/logback-spring.xml -L -k https://github.com/camueller/SmartApplianceEnabler/raw/master/run/logback-spring.xml

COPY SmartApplianceEnabler-$SAE_VERSION.war $SAE_INSTALL_DIR/SmartApplianceEnabler-$SAE_VERSION.war

CMD java $JAVA_OPTS -Djava.awt.headless=true -Xmx256m -Dsae.home=$SAE_HOME -Dlogging.config=$SAE_INSTALL_DIR/logback-spring.xml -jar $SAE_INSTALL_DIR/SmartApplianceEnabler-$SAE_VERSION.war