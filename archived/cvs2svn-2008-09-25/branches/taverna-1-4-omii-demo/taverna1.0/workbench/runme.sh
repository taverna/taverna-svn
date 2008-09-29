#!/bin/sh

## resolve links - $0 may be a symlink
PRG="$0"
progname=`basename "$0"`
saveddir=`pwd`

# need this for relative symlinks
cd $(dirname "$PRG")
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
done
  
TAVERNA_HOME=`dirname "$PRG"`

cd "$saveddir"

OMII_CLIENT_HOME=@OMII_CLIENT_HOME@

TAVERNA_OMII_CONF=$OMII_CLIENT_HOME/conf

TAVERNA_OMII_JARS=$OMII_CLIENT_HOME/lib/bouncycastle-jce-jdk13-119.jar
TAVERNA_OMII_JARS=$TAVERNA_OMII_JARS:$OMII_CLIENT_HOME/lib/castor-0.9.7.jar
TAVERNA_OMII_JARS=$TAVERNA_OMII_JARS:$OMII_CLIENT_HOME/lib/itinnov-grid-utils-omii1.jar
TAVERNA_OMII_JARS=$TAVERNA_OMII_JARS:$OMII_CLIENT_HOME/lib/opensaml-1.0.1.jar
TAVERNA_OMII_JARS=$TAVERNA_OMII_JARS:$OMII_CLIENT_HOME/lib/wss4j-itinnov-4.jar
TAVERNA_OMII_JARS=$TAVERNA_OMII_JARS:$OMII_CLIENT_HOME/lib/xmlsec-1.2.1.jar

TAVERNA_OMII_FILES=$TAVERNA_OMII_CONF:$TAVERNA_OMII_JARS

TAVERNA_OMII_OPTS=-Daxis.ClientConfigFile=$OMII_CLIENT_HOME/conf/default-client-config.wsdd
TAVERNA_OMII_OPTS="$TAVERNA_OMII_OPTS -Djava.endorsed.dirs=$OMII_CLIENT_HOME/endorsed"
TAVERNA_OMII_OPTS="$TAVERNA_OMII_OPTS -Dtaverna.path=$TAVERNA_OMII_FILES"

java @MAC-OPTS@ -Xmx300m -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client $TAVERNA_OMII_OPTS -jar $TAVERNA_HOME/taverna-launcher-1.4.1.jar