#!/bin/sh
JAR=`ls SmartApplianceEnabler-*.jar`
CONF_DIR=.

usage(){
  echo "Usage: $0 [OPTION]"
  echo "  -c <directory>      Directory containing configuration files" 
  echo "  -d                  Enable debug output to console" 
  echo "  -x                  Enable JVM debugging" 
  exit 1
}


while getopts ':c:dx' OPTION ; do
  case "$OPTION" in
    c) CONF_DIR=$OPTARG;;
    d) DEBUG="-Dsae.debug";;
    x) JVM_DEBUG="-Xdebug -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n";;
    *) usage
  esac
done
RUN="sudo java $JVM_DEBUG -Dappliance.dir=$CONF_DIR $DEBUG -jar $JAR"
echo $RUN
$RUN
