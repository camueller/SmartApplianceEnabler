FROM dpsmyth/raspberrypi3-alpine-java

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV PATH="/usr/bin:${PATH}"

EXPOSE 8080

# download latest SmartApplianceEnabler release from GitHub to /opt (directory has already been created by parent image)
#RUN apk upgrade --update && apk add --update jq wget && rm -rf /var/cache/apk/* && echo "#!/bin/bash" > /tmp/install.sh && \
#    echo "wget -q -O - https://api.github.com/repos/camueller/SmartApplianceEnabler/releases/latest | jq -r '.assets[0].name' | sed 's/.*-\(.*\)\..ar/\1/' | awk '{print \"https://github.com/camueller/SmartApplianceEnabler/releases/download/v\"\$1\"/SmartApplianceEnabler-\"\$1\".jar\"}' | xargs wget -O /opt/SmartApplianceEnabler.war" >> /tmp/install.sh && \
#    cat /tmp/install.sh && chmod +x /tmp/install.sh && /bin/bash -c /tmp/install.sh

# until the next release we have to download the snapshot instead
RUN apk upgrade --update && apk add --update jq wget && rm -rf /var/cache/apk/* && echo "#!/bin/bash" > /tmp/install.sh && \
    echo "wget https://github.com/camueller/SmartApplianceEnabler/raw/master/snapshots/SmartApplianceEnabler-1.2.0-SNAPSHOT.war -O /opt/SmartApplianceEnabler.war" >> /tmp/install.sh && \
    cat /tmp/install.sh && chmod +x /tmp/install.sh && /bin/bash -c /tmp/install.sh

CMD java -Djava.awt.headless=true -Xmx256m -Dsae.home=/app -Dsae.discovery.disable=true \
    -Dlogging.config=/app/logback-spring.xml -jar /opt/SmartApplianceEnabler.war
