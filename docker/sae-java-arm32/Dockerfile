FROM easypi/alpine-arm

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

ENV JAVA_VERSION_MAJOR=8 \
    JAVA_VERSION_MINOR=65 \
    JAVA_VERSION_BUILD=17 \
    JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/jre/bin \
    GLIBC_VERSION_MAJOR=2.22 \
    GLIBC_VERSION=2.22-r8 \
    LANG=C.UTF-8

RUN set -ex && \
    apk upgrade --update && \
    apk add --update libstdc++ curl ca-certificates bash gzip && \
    for pkg in glibc-${GLIBC_VERSION} glibc-bin-${GLIBC_VERSION} glibc-i18n-${GLIBC_VERSION}; do curl -sSL https://github.com/armhf-docker-library/alpine-pkg-glibc/releases/download/${GLIBC_VERSION_MAJOR}/${pkg}.apk -o /tmp/${pkg}.apk; done && \
    apk add --allow-untrusted /tmp/*.apk && \
    rm -v /tmp/*.apk && \
    ( /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true ) && \
    echo "export LANG=C.UTF-8" > /etc/profile.d/locale.sh && \
    /usr/glibc-compat/sbin/ldconfig /lib /usr/glibc-compat/lib && \
    curl -o /tmp/java.tar.gz -L -k https://www.dropbox.com/s/9ywkr1rpxpf83c5/java.tar.gz?dl=0 && \
    tar -C /tmp -xvzf /tmp/java.tar.gz && \
    mkdir -p /opt/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} && \
    mv /tmp/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR}/jre /opt/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} && \
    ln -s /opt/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} ${JAVA_HOME} && \
    rm -rf /tmp/* \
           /var/cache/apk/*
