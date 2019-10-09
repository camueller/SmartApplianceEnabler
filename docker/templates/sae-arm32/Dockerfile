FROM avanux/smartapplianceenabler-java-arm32

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV PATH="/usr/bin:${PATH}"
ENV SAE_VERSION=@project.version@
ENV JAVA_OPTS=""

EXPOSE 8080

# download latest SmartApplianceEnabler release from GitHub to /opt (directory has already been created by parent image)
#RUN apk upgrade --update && apk add --update jq wget && rm -rf /var/cache/apk/* && echo "#!/bin/bash" > /tmp/install.sh && \
#    echo "wget -q -O - https://api.github.com/repos/camueller/SmartApplianceEnabler/releases/latest | jq -r '.assets[0].name' | sed 's/.*-\(.*\)\..ar/\1/' | awk '{print \"https://github.com/camueller/SmartApplianceEnabler/releases/download/v\"\$1\"/SmartApplianceEnabler-\"\$1\".jar\"}' | xargs wget -O /opt/SmartApplianceEnabler.war" >> /tmp/install.sh && \
#    cat /tmp/install.sh && chmod +x /tmp/install.sh && /bin/bash -c /tmp/install.sh

RUN apk upgrade --update && apk add --update jq curl tzdata && \
    cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime && echo "Europe/Berlin" > /etc/timezone && apk del tzdata && \
    curl -o /opt/SmartApplianceEnabler.war -L -k https://github.com/camueller/SmartApplianceEnabler/releases/download/v1.4.14/SmartApplianceEnabler-1.4.14-SNAPSHOT.war

CMD java $JAVA_OPTS -Djava.awt.headless=true -Xmx256m -Dsae.home=/app -Dlogging.config=/app/logback-spring.xml -jar /opt/SmartApplianceEnabler.war

