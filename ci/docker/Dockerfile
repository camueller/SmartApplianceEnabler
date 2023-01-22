# Add "ps" to Jenkins Docker image in order to avoid error:
# Error: spawn ps ENOENT

# the jenkins version to be used
FROM jenkins/jenkins:2.361.4-lts

MAINTAINER Axel Mueller <axel.mueller@avanux.de>

# switch to user root ...
USER root
# ... in order to be able to install "ps" and required libraries
RUN apt-get update && apt-get -y install procps

# switch back to user of base docker image
USER jenkins
