FROM avanux/smartapplianceenabler

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV TZ="Europe/Berlin"

RUN apk upgrade --update && apk add --update tzdata && cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime && echo "Europe/Berlin" > /etc/timezone && apk del tzdata

