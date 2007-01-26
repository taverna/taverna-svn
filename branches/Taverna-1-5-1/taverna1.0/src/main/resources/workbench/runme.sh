#!/bin/sh

## resolve links - $0 may be a symlink
PRG="$0"
progname=`basename "$0"`
saveddir=`pwd`

# need this for relative symlinks
cd "$(dirname "$PRG")"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
done
  
TAVERNA_HOME="`dirname "$PRG"`"
cd "$saveddir"

ARGS="-Xmx300m"
ARGS="$ARGS -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader"
ARGS="$ARGS -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client"

#uncomment and complete the next line and complete to change the directory taverna downloads jars to
#ARGS="$ARGS -Dtaverna.repository=<directory>"

#uncomment and complete the next line to set http proxy settings, and the 2nd line if authentication is required
#ARGS="$ARGS -Dhttp.proxyHost=<hostname or ip address> -Dhttp.proxyPort=<port>"
#ARGS="$ARGS -Dhttp.proxyUser=<username> -Dhttp.proxyPassword=<password>"

java $ARGS -jar $TAVERNA_HOME/taverna-bootstrap-1.5.1.jar $@
