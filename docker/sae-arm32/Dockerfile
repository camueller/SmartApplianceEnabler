FROM avanux/smartapplianceenabler-java-arm32:8.65.17

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV PATH="/usr/bin:${PATH}"
ENV JAVA_OPTS=""
ENV SAE_INSTALL_DIR=/opt/sae
ENV SAE_HOME=/opt/sae/data

EXPOSE 8080

RUN apk upgrade --update && apk add --update jq curl tzdata wiringpi && \
    cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime && echo "Europe/Berlin" > /etc/timezone && apk del tzdata && \
    mkdir -p $SAE_INSTALL_DIR && \
    curl -o $SAE_INSTALL_DIR/logback-spring.xml -L -k https://github.com/camueller/SmartApplianceEnabler/raw/master/logback-spring.xml && \
    curl -o $SAE_INSTALL_DIR/SmartApplianceEnabler.war -L -k https://github.com/camueller/SmartApplianceEnabler/releases/download/1.4.19/SmartApplianceEnabler-1.4.19.war

CMD java $JAVA_OPTS -Djava.awt.headless=true -Xmx256m -Dsae.home=$SAE_HOME -Dlogging.config=$SAE_INSTALL_DIR/logback-spring.xml -jar $SAE_INSTALL_DIR/SmartApplianceEnabler.war
